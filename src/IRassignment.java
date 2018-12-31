import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.InputMismatchException;

import java.io.BufferedReader;
import java.io.File;
import java.util.Random;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import java.io.PrintStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class IRassignment {
    
    public static void main(String args[]) throws FileNotFoundException, IOException, ClassNotFoundException
    {
    	//Parse the Given Cranfield Collection
    	Part1.generateCorpCol(Part1.queryProcessing());
		
        
    	// load the matrix only if there is no tfidfmatrix.zip	
        if (!Part1.dataPreProcessing()) {
        	//calculates tfidf
        	Part1.tfidfMatrix();
        }
        // process the query manually
        Part1.processQuery(Part1.query);
        
		//calculated cosine similarity   
        Part1.cosine();	
		
		String part1_name = "output/Part1.txt";
		try{
				PrintStream out = new PrintStream(new FileOutputStream(part1_name));
				System.setOut(out);
		} catch (FileNotFoundException fnfe) {
			System.out.println(fnfe);
		}
		System.out.println("Vector Length of each Document:");
		for(int i=0;i < Part1.DocsVectorLength.size();i++)
		{
			System.out.println(Part1.DocsVectorLength.get(i));
        }		
    }
}