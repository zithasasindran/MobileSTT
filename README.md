# MobileSTT
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
IMPORTANT FILES IN ASSETS:
**DO NOT RENAME THE FOLLOWING FILES IF REPLACING**
=======================================================
1. newmodel.ckpt
	- Current trained weights of the model
2. MYMODEL.tflite
	- TFlite model created in python
3. ground_truth.csv
	- 60 training + 10 validation sentences
4. onDeviceTraining_dataset.csv
	- empty csv that will be copied to local dir to be added by user
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

HOW THE APPLICATION WORKS (in terms of training)
======================================================

1. SpeechtoTextMainActivity acts as the main() for the application
2. if train button is clicked TrainService is invoked
3. TrainService creates a service that executes Train.trainthemodel()
4. in trainthemodel() first it checks if ckpt exists ... if not copy ckpt from assets to local dir
5. next it reads all the WAV files the user has created in Music/dataset
	**USER MUST CREATE MIN OF 70 WAV FILES**
 	-First melConversion process outputs a dynamic melspectogram in a 2 dimensional float array variable named -> melInput
	- Since we initially needed (1,500,80,1) 4 dimensional array 
		i) we make variable (1,500,80,1) called -> temp1bs with -13.815511
		ii) next we read melInput and only copy till 1,500,80,1 from it to temp1bs
		iii)Similiarly since we need (5,500,80,1) for training we apply the same method but using batchnumber variable to fill up till 5 per batch	
		
		**IF TIMEFRAMES NEED TO BE MODIFIED PLEASE CHANGE ALL 500's to NEW TIMEFRAME FROM LINES 304-380 IN THE CODE*
		
		
6. Stores all the melspectograms in ARRAYLISTS 
	<float[5][500][80][1]>batches(for training) 
	<float[1][500][80][1]>batcheswer(for inference)

7. Stores all the groundtruths in ARRAYLISTS
	<int[5][150]> batcheslabels;
        <String> stringLables;
        
        
8. LOADING MODEL
	i. Once the interpreter for the model is created
		- first we reload the weights newmodel.ckpt from LOCAL_DIR by invoking "restore" signature
		
		- we make initial cer and wer calculation by calling generateinference() which invokes "infer" signature
		
		- We Train using the weights by invoking "train" signature
		
		- we make cer and wer calculation again in a while loop
		
	ii. We save the weights after every epoch on newmodel.ckpt
	
	** IF YOU WANT TO RESTART TRAINING FROM BEGINING PLEASE UNINSTALL AND REINSTALL TO LOAD FREASH NEW WEIGHTS**

9. Service ends when training ends
		
		
