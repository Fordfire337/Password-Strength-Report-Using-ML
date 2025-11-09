# Password-Strength-Report-Using-ML
Created During Troy Universitys hackathon (11/8/2025) by Justin Williams
This project uses java and python to build a ML password strength reporter
I have been unable to get a true feel for the ML due to my laptop being relitivly slow (I stopped training after 125 of 15K (epoch 1 of 5))
Java: Buildpath -> classpath had json-20250517.jar
Makesure to pip install all required modules for Python

Seq of Operation
Python First:
              Preprocess_passwords.py (Should get x and y.npy (one is significaly larger))
              train_lstm.py (train the module, this is where i stopped short due to lack of time and processing power, stop program and use model.save("password_lstm_model.keras") to export what is made so far.)
              password_lstm_service.py (This is the service)

Java:
              replace string in the following with deired password to test: String password = "123";
              Run:
              Example OutPut:
Password Strength Report:
------------------------
Complexity: Weak
Predictability: Weak (dictionary word: 2)
ML Model Score (0=weak,2=strong): 2
ML Probabilities: [0.00004272478327038698,0.00005605418482446112,0.999901294708252]    #(This is [weak_prob, moderate_prob, strong_prob] [0.00004, 0.00005, 0.9999] → extremely confident it’s strong, (Edit: yes 123 is not a strong password but from my understanding it is due to only training the modules on longer passwords, along with not many datasets))


Breach Check: Compromised! Found in breaches 15100375 times.
Overall Password Strength: Weak

              
              
