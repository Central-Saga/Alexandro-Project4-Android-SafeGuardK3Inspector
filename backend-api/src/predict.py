import joblib
import pandas as pd

# Load artifacts
model = joblib.load('artifacts/model.pkl')
preprocessor = joblib.load('artifacts/preprocessor.pkl')

def predict(input_df: pd.DataFrame):
    """
    input_df: Raw DataFrame (belum dipreprocess)
    return: list/array hasil prediksi
    """
    X_processed = preprocessor.transform(input_df)
    predictions = model.predict(X_processed)
    return predictions
