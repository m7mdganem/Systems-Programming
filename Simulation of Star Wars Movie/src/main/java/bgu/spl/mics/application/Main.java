package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;

import java.io.FileWriter;
import java.io.IOException;

/** This is the Main class of the application. You should parse the input file,
 * create the different components of the application, and run the system.
 * In the end, you should output a JSON.
 */
public class Main {
	public static void main(String[] args) {

		String input = args[0];
		String output = args[1];
		Diary diary =  Diary.getInstance();
		Ewoks ewoks = Ewoks.getInstance();

		try {

			//initialize our heroes
			Input InputJson = JsonInputReader.getInputFromJson(input);//read input
			ewoks.buildEwoksList(InputJson.getEwoks());//build the list of ewoks
			LandoMicroservice Lando = new LandoMicroservice(InputJson.getLando());//initialize Lando
			R2D2Microservice R2D2 = new R2D2Microservice(InputJson.getR2D2());//initialize R2D2
			LeiaMicroservice Leia = new LeiaMicroservice(InputJson.getAttacks());//initialize Leia
			HanSoloMicroservice HanSolo = new HanSoloMicroservice();//initialize HanSolo
			C3POMicroservice C3PO = new C3POMicroservice();//initialize C3PO

			//create all threads
			Thread LandoThread = new Thread(Lando);//create Lando Thread
			Thread R2D2Thread = new Thread(R2D2);//crate R2D2 Thread
			Thread HanSoloThread = new Thread(HanSolo);//create HanSolo Thread
			Thread C3POThread = new Thread(C3PO);//create C3PO Thread
			Thread LeiaThread = new Thread(Leia);//create Leia Thread

			//start all threads
			LandoThread.start();
			R2D2Thread.start();
			HanSoloThread.start();
			C3POThread.start();
			LeiaThread.start();

			//join until all threads terminate
			try {
				LandoThread.join();
				R2D2Thread.join();
				HanSoloThread.join();
				C3POThread.join();
				LeiaThread.join();
			}catch(InterruptedException e){
			}

			//print to output
			Gson outputGson = new GsonBuilder().setPrettyPrinting().create();//create the output gson
			FileWriter writer = new FileWriter(output);//create file writer
			outputGson.toJson(diary,writer);//write the diary to the output file
			writer.flush();
			writer.close();

		}catch (IOException e){
		}

	}

}
