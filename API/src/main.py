import asyncio
import traceback
from fastapi import Request
import json
import mysql.connector
from fastapi import FastAPI, HTTPException, WebSocket

app = FastAPI()

db = mysql.connector.connect(
    host="localhost",
    user="root",
    password="foobar123",
    database="TorCord")

@app.get("/")
def read_root():
    return {"Hello": "World"}


@app.get("/msg/{id}")
def read_item(id: int):
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM messages WHERE msgID = %s", (id,))
    result = cursor.fetchone()
    if result is None:
        raise HTTPException(status_code=404, detail="Item not found")
    return result


@app.get("/msg")
async def read_items():
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM messages")
    result = cursor.fetchall()
    return result


@app.post("/msg")
async def create_item(request: Request):
    try:
        data = await request.json()
        userID = data['userID']
        content = data['content']
    except Exception as e:
        print(f"An error occurred while parsing the request data: {e}")
        print(f"Received data: {await request.body()}")
        raise HTTPException(status_code=400, detail="Invalid request data")

    cursor = db.cursor(dictionary=True)
    cursor.execute("INSERT INTO messages (userID, content) VALUES (%s, %s)", (userID, content))
    db.commit()
    cursor.execute("SELECT LAST_INSERT_ID()")
    result = cursor.fetchone()
    return {"userID": userID, "content": content}


@app.post("/msg/{id}")
async def edit_item(id: int, userID: str, content: str):
    cursor = db.cursor(dictionary=True)
    cursor.execute("UPDATE messages SET userID = %s, content = %s WHERE msgID = %s", (userID, content, id))
    db.commit()
    return {"msgID": id, "userID": userID, "content": content}


@app.websocket("/msgStream")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    # await asyncio.sleep(5) # Uncomment this line to test client thread load handling
    sent_message_ids = set()  # Initialize sent_message_ids as an empty set
    try:
        while True:
            cursor = db.cursor(dictionary=True)
            cursor.execute("SELECT * FROM messages")
            result = cursor.fetchall()
            for row in result:
                if row['msgID'] in sent_message_ids:
                    continue  # Skip this message if it has been sent before
                # Convert datetime to string
                row['datetime'] = row['datetime'].isoformat()
                await websocket.send_json(row)
                sent_message_ids.add(row['msgID'])  # Add the ID of the sent message to sent_message_ids
            await asyncio.sleep(1)
    except Exception as e:
        print(f"An error occurred: {e}")
        traceback.print_exc()



