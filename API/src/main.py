import mysql.connector
from fastapi import FastAPI, HTTPException

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
def read_items():
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM messages")
    result = cursor.fetchall()
    return result


@app.post("/msg")
def create_item(userID: str, content: str):
    cursor = db.cursor(dictionary=True)
    cursor.execute("INSERT INTO messages (userID, content) VALUES (%s, %s)", (userID, content))
    db.commit()
    return {"userID": userID, "content": content}


@app.post("/msg/{id}")
def edit_item(id: int, userID: str, content: str):
    cursor = db.cursor(dictionary=True)
    cursor.execute("UPDATE messages SET userID = %s, content = %s WHERE msgID = %s", (userID, content, id))
    db.commit()
    return {"msgID": id, "userID": userID, "content": content}
