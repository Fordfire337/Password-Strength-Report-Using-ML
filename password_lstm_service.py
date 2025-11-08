from flask import Flask, request, jsonify
import tensorflow as tf
from tensorflow.keras.preprocessing.sequence import pad_sequences
import pickle
import numpy as np

app = Flask(__name__)

# Load trained model
import keras

model = tf.keras.models.load_model("password_lstm_model.keras")

# Load tokenizer
with open("tokenizer.pkl", "rb") as f:
    tokenizer = pickle.load(f)

max_len = model.input_shape[1]

@app.route("/predict", methods=["POST"])
def predict():
    password = request.json.get("password", "")
    seq = tokenizer.texts_to_sequences([password])
    padded = pad_sequences(seq, maxlen=max_len, padding='post')
    pred_proba = model.predict(padded)[0]
    pred_class = int(pred_proba.argmax())
    return jsonify({"score": pred_class, "probabilities": pred_proba.tolist()})

if __name__ == "__main__":
    app.run(port=5000)
