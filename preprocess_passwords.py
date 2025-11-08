import numpy as np
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from sklearn.model_selection import train_test_split
import pickle

# Load your dataset
with open(r"C:\Users\susie\Downloads\rockyou_2025_01.txt", "r", encoding="latin-1") as f:
    passwords = [line.strip() for line in f if line.strip()]

# Labels: 0=weak,1=moderate,2=strong
top_10k = set(passwords[:10])#1k and 10k
top_100k = set(passwords[:100])
labels = [0 if p in top_10k else 1 if p in top_100k else 2 for p in passwords]

# Character-level tokenizer
tokenizer = Tokenizer(char_level=True)
tokenizer.fit_on_texts(passwords)
sequences = tokenizer.texts_to_sequences(passwords)

# Pad sequences
max_len = max(len(p) for p in passwords)
X = pad_sequences(sequences, maxlen=max_len, padding='post')
y = np.array(labels)

# Save preprocessed data and tokenizer
with open("tokenizer.pkl", "wb") as f:
    pickle.dump(tokenizer, f)
np.save("X.npy", X)
np.save("y.npy", y)
