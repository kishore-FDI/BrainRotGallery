from flask import Flask, jsonify

app = Flask(__name__)

@app.route("/")
def read_root():
    return jsonify({"message": "Hello from Flask on Vercel!"})
