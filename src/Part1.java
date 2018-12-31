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
import java.io.*;

public class Part1 {
	// A list of String to hold all parsed documents
	public static List<String> documents = new ArrayList<String>();
	
	//A List of String that will hold all terms of each document
	public static List<String[]> tokensPerDocument = new ArrayList<String[]>();
	
	//A List of String that will hold all terms of all documents
	public static List<String> allTokens = new ArrayList<String>();
	
	//A Floating List to hold tf-idf matrix
	public static List<float[]> tfidfDocsVector = new ArrayList<float[]>();
	
	//A Floating List to hold Doc Vector Length
	public static List<Float> DocsVectorLength = new ArrayList<Float>();
	
	// A String to hold user input query
	public static String query = "";
	
	// asking the user to input a query
	public static String queryProcessing() {
		Scanner input = new Scanner( System.in );
		System.out.print("Enter Query: ");
		query = input.nextLine();
		input.close();
		return query;
	}
	
	public static void generateCorpCol(String query) throws IOException {
		final byte separator = 3;
		String inputFile="data/cranfield_collection.txt";
		BufferedReader br = new BufferedReader(new FileReader(inputFile),15000);
		List<String> data = new ArrayList<String>();
		// declare variables where i represents Number
		// t for title
		// a for author
		// b for Publication
		// w for content
		int i,t,a,b,w = 0;
		
		//For get all stopwords from the file into a string
		String inputstopwordsFile="data/stopwords.txt";
		BufferedReader br2 = new BufferedReader(new FileReader(new File(inputstopwordsFile)));
		String line2=null;
		List<String> stopwords = new ArrayList<String>();
		try {
			while((line2 = br2.readLine()) != null){
			   stopwords.add(line2);
		    }
		} catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
			br2.close();
        }
		//System.out.println(stopwords);
		
		@SuppressWarnings("unused")
		String sI, sT, sA, sB, sW = "";
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			String allContent = sb.toString();
			i = allContent.indexOf(".I");
			// logic for parsing
			while (i != -1) {
				w = allContent.indexOf(".W");
				t = allContent.indexOf(".T");
				a = allContent.indexOf(".A");
				b = allContent.indexOf(".B");
				w = allContent.indexOf(".W");
				sI = allContent.substring(i + separator, t);
		        sT = allContent.substring(t + separator, a);
		        sA = allContent.substring(a + separator, b);
		        sB = allContent.substring(b + separator, w);
				allContent = allContent.substring(w+separator);
				i = allContent.indexOf(".I");
				if (i != -1) {
					sW = allContent.substring(0 , allContent.indexOf(".I"));
				}
				else {
					sW = allContent.substring(0);
				}
				
				// combining all strings
				data.add(sT + " " + sA + " " + sB + " " + sW);
			}
		} catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
			br.close();
        }
		Iterator<String> itr = data.iterator();
		while(itr.hasNext()) {
			String element = (String) itr.next();
			String word = element;
			word = word.replaceAll("\r", "");
			word = word.replaceAll("\t", "");
			word = word.replaceAll("\n", "");
	
			// removing stopwords
			for(String stopWord : stopwords){
				word = word.toString().replaceAll(" "+ stopWord + " ", " ");
			}
			Stemmer stemObj = new Stemmer();
			char[] chars = word.toCharArray();
			int length = chars.length;
			stemObj.add(chars,length);
			stemObj.stem();
			String res = stemObj.toString();
			documents.add(res);
		}

		for (int k = 0; k<documents.size();k++) {
			String[] tokenizedTerms = documents.get(k).replaceAll("[\\W&&[^\\s]]", "").split("\\W+");
			
			for (String token : tokenizedTerms) {

				// for adding distinct tokens
				if (!allTokens.contains(token)) {  
					allTokens.add(token);
				}
				
			}

			// tokens by each documents
			tokensPerDocument.add(tokenizedTerms);
		}
	}
	@SuppressWarnings("unchecked")
	public static boolean dataPreProcessing() throws IOException, ClassNotFoundException {
		boolean valueReturn = false;
		if (new File("tfidfmatrix.zip").isFile()) {
			unZipIt("tfidfmatrix.zip",System.getProperty("user.dir"));
			FileInputStream file_input = new FileInputStream("tfidfmatrix.data");
			ObjectInputStream object_input = new ObjectInputStream (file_input);
			Object obj = object_input.readObject();
			object_input.close();
			if(obj instanceof ArrayList)
			{
				tfidfDocsVector = (ArrayList<float[]>) obj;
			}
			valueReturn = true;
		}
		return valueReturn;
	}	

	public static void dataProcessed() throws IOException{
		FileOutputStream file_output = new FileOutputStream("tfidfmatrix.data");
		ObjectOutputStream object_output = new ObjectOutputStream (file_output);
		object_output.writeObject ( tfidfDocsVector );
		object_output.close();
	}		
			
	public static void unZipIt(String zipFile, String outputFolder){		
		byte[] buffer = new byte[1024];	
		try {	
			File folder = new File(outputFolder);
			if(!folder.exists()){
				folder.mkdir();
			}
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze = zis.getNextEntry();
			while(ze!=null){
				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile); 
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		} catch(IOException ex){
			ex.printStackTrace();
		}
	}

	public static void tfidfMatrix() throws IOException {
		for (String[] docTokensArray : tokensPerDocument) {
			float[] tfidfvectors = new float[allTokens.size()];
			int count = 0;
			for (String tokens : allTokens) {
				tfidfvectors[count] = tfCalculator(docTokensArray, tokens) * idfCalculator(tokensPerDocument, tokens);
				count++;
			}
			//Document Vectors Store
			tfidfDocsVector.add(tfidfvectors);
		}
	}	

	// get and print the similarity
	public static void cosine() {
		HashMap<String, Float> result = new HashMap<String, Float>();
		ValueComparator comp =  new ValueComparator(result);
		String filename = "output/Output.txt";
		try{
			if(!new File(filename).exists()) {
				PrintStream out = new PrintStream(new FileOutputStream(filename));
				System.setOut(out);
			}
			else{
				int random1 = (int)(Math.random() * 50 + 1);	
				filename = "output/Output" + Integer.toString(random1) + ".txt";
				PrintStream out = new PrintStream(new FileOutputStream(filename));
				System.setOut(out);	
			}
		} catch (FileNotFoundException fnfe) {
			System.out.println(fnfe);
		}
		//sorting the result
		TreeMap<String,Float> sorted_map = new TreeMap<String,Float>(comp);
		float cosine = 0;
		for (int i = 0; i < tfidfDocsVector.size(); i++) {
			cosine = getSimilarity(tfidfDocsVector.get(tfidfDocsVector.size()-1), tfidfDocsVector.get(i));
			result.put(String.valueOf(i+1),cosine);
		}
		sorted_map.putAll(result);
		System.out.println();
		System.out.println("Showing the 100 Best Results according to the given query");
		System.out.println();
		for (byte i = 1; i < 101; i++) {
			float v = (float) sorted_map.values().toArray()[i];
			if (Float.isNaN(v)) {
				v = 00;
			}
			System.out.println(i + " " + sorted_map.keySet().toArray()[i] + " " + v);
		}	
		File file = new File("tfidfmatrix.data");
		file.delete();
	}

	//Add query to the table
	public static void processQuery(String query) {
		List<String[]> tokensDocsQuery = new ArrayList<String[]>();
		//get individual query term
		String[] queryTokens = query.replaceAll("[\\W&&[^\\s]]", "").split("\\W+");
		tokensDocsQuery.add(queryTokens);
		for(String token : queryTokens){
			if(!allTokens.contains(token)){
				allTokens.add(token);
			}
		}
		//Add query to document
		tokensPerDocument.add(queryTokens);
		float[] tfidfvectors = new float[allTokens.size()];
		int count = 0;
		for (String[] docTokensArray : tokensDocsQuery) { 
			for (String tokens : allTokens) {
				tfidfvectors[count] = tfCalculator(docTokensArray, tokens) * idfCalculator(tokensPerDocument, tokens);
				count++;
			}	
		}
		tfidfDocsVector.add(tfidfvectors);
		
	}	
	// Calculate the tf
	public static float tfCalculator(String[] totaltokens, String tokenToCheck) {
		float occurrences = 0;
		for (String separatedTerm : totaltokens) {
			if (separatedTerm.equalsIgnoreCase(tokenToCheck)) {
				occurrences++;
			}
		}
		return occurrences/totaltokens.length;
	}
	
	// Calculate the idf
	public static float idfCalculator(List<String[]> allTokens, String check) {
		float count = 0;
		for (String[] arrayTokens : allTokens) {
			for (String separatedTerms : arrayTokens) {
				if (separatedTerms.equalsIgnoreCase(check)) {
					count++;
					break;
				}
			}
		}
		return (float)Math.log(allTokens.size() / count);
	}

	// getting the similarity between each document
	public static float getSimilarity(float[] docVector1, float[] docVector2) {
		float qd = 0;
		float queyP = 0;
		float docP = 0;
		float cosineResult = 0;
		for (int i = 0; i <docVector2.length; i++)
		{
			// adding all multiplication of array in the same index
			qd += docVector1[i] * docVector2[i]; 
			// powering query vector by 2
			queyP += Math.pow(docVector1[i], 2);
			// powering document vector by 2
			docP += Math.pow(docVector2[i], 2);
		}
		// getting the lenght vector
		queyP = (float)Math.sqrt(queyP);
		// getting the document  vector length
		docP = (float)Math.sqrt(docP);
		//Adding these values to List for result of Part 1
		DocsVectorLength.add(docP);

		// if both values are not 0, there were no occurrences at all
		// if != 0, it is noa NaN so there is a smilarity to show
		if (queyP != 0.0 | docP != 0.0) {
			cosineResult = qd / (queyP * docP);
		} else {
			// Set to 0 manually since it is NaN by default
			return 0;
		}
		// returning the similarity
		return cosineResult;
	}
}


/*

   Porter stemmer in Java. The original paper is in

       Porter, 1980, An algorithm for suffix stripping, Program, Vol. 14,
       no. 3, pp 130-137,

   See also http://www.tartarus.org/~martin/PorterStemmer

   History:

   Release 1

   Bug 1 (reported by Gonzalo Parra 16/10/99) fixed as marked below.
   The words 'aed', 'eed', 'oed' leave k at 'a' for step 3, and b[k-1]
   is then out outside the bounds of b.

   Release 2

   Similarly,

   Bug 2 (reported by Steve Dyrdahl 22/2/00) fixed as marked below.
   'ion' by itself leaves j = -1 in the test for 'ion' in step 5, and
   b[j] is then outside the bounds of b.

   Release 3

   Considerably revised 4/9/00 in the light of many helpful suggestions
   from Brian Goetz of Quiotix Corporation (brian@quiotix.com).

   Release 4

*/

/**
  * Stemmer, implementing the Porter Stemming Algorithm
  *
  * The Stemmer class transforms a word into its root form.  The input
  * word can be provided a character at time (by calling add()), or at once
  * by calling one of the various stem(something) methods.
  */

class Stemmer
{  private char[] b;
   private int i,     /* offset into b */
               i_end, /* offset to end of stemmed word */
               j, k;
   private static final int INC = 50;
                     /* unit of size whereby b is increased */
   public Stemmer()
   {  b = new char[INC];
      i = 0;
      i_end = 0;
   }

   /**
    * Add a character to the word being stemmed.  When you are finished
    * adding characters, you can call stem(void) to stem the word.
    */

   public void add(char ch)
   {  if (i == b.length)
      {  char[] new_b = new char[i+INC];
         for (int c = 0; c < i; c++) new_b[c] = b[c];
         b = new_b;
      }
      b[i++] = ch;
   }


   /** Adds wLen characters to the word being stemmed contained in a portion
    * of a char[] array. This is like repeated calls of add(char ch), but
    * faster.
    */

   public void add(char[] w, int wLen)
   {  if (i+wLen >= b.length)
      {  char[] new_b = new char[i+wLen+INC];
         for (int c = 0; c < i; c++) new_b[c] = b[c];
         b = new_b;
      }
      for (int c = 0; c < wLen; c++) b[i++] = w[c];
   }

   /**
    * After a word has been stemmed, it can be retrieved by toString(),
    * or a reference to the internal buffer can be retrieved by getResultBuffer
    * and getResultLength (which is generally more efficient.)
    */
   public String toString() { return new String(b,0,i_end); }

   /**
    * Returns the length of the word resulting from the stemming process.
    */
   public int getResultLength() { return i_end; }

   /**
    * Returns a reference to a character buffer containing the results of
    * the stemming process.  You also need to consult getResultLength()
    * to determine the length of the result.
    */
   public char[] getResultBuffer() { return b; }

   /* cons(i) is true <=> b[i] is a consonant. */

   private final boolean cons(int i)
   {  switch (b[i])
      {  case 'a': case 'e': case 'i': case 'o': case 'u': return false;
         case 'y': return (i==0) ? true : !cons(i-1);
         default: return true;
      }
   }

   /* m() measures the number of consonant sequences between 0 and j. if c is
      a consonant sequence and v a vowel sequence, and <..> indicates arbitrary
      presence,

         <c><v>       gives 0
         <c>vc<v>     gives 1
         <c>vcvc<v>   gives 2
         <c>vcvcvc<v> gives 3
         ....
   */

   private final int m()
   {  int n = 0;
      int i = 0;
      while(true)
      {  if (i > j) return n;
         if (! cons(i)) break; i++;
      }
      i++;
      while(true)
      {  while(true)
         {  if (i > j) return n;
               if (cons(i)) break;
               i++;
         }
         i++;
         n++;
         while(true)
         {  if (i > j) return n;
            if (! cons(i)) break;
            i++;
         }
         i++;
       }
   }

   /* vowelinstem() is true <=> 0,...j contains a vowel */

   private final boolean vowelinstem()
   {  int i; for (i = 0; i <= j; i++) if (! cons(i)) return true;
      return false;
   }

   /* doublec(j) is true <=> j,(j-1) contain a double consonant. */

   private final boolean doublec(int j)
   {  if (j < 1) return false;
      if (b[j] != b[j-1]) return false;
      return cons(j);
   }

   /* cvc(i) is true <=> i-2,i-1,i has the form consonant - vowel - consonant
      and also if the second c is not w,x or y. this is used when trying to
      restore an e at the end of a short word. e.g.

         cav(e), lov(e), hop(e), crim(e), but
         snow, box, tray.

   */

   private final boolean cvc(int i)
   {  if (i < 2 || !cons(i) || cons(i-1) || !cons(i-2)) return false;
      {  int ch = b[i];
         if (ch == 'w' || ch == 'x' || ch == 'y') return false;
      }
      return true;
   }

   private final boolean ends(String s)
   {  int l = s.length();
      int o = k-l+1;
      if (o < 0) return false;
      for (int i = 0; i < l; i++) if (b[o+i] != s.charAt(i)) return false;
      j = k-l;
      return true;
   }

   /* setto(s) sets (j+1),...k to the characters in the string s, readjusting
      k. */

   private final void setto(String s)
   {  int l = s.length();
      int o = j+1;
      for (int i = 0; i < l; i++) b[o+i] = s.charAt(i);
      k = j+l;
   }

   /* r(s) is used further down. */

   private final void r(String s) { if (m() > 0) setto(s); }

   /* step1() gets rid of plurals and -ed or -ing. e.g.

          caresses  ->  caress
          ponies    ->  poni
          ties      ->  ti
          caress    ->  caress
          cats      ->  cat

          feed      ->  feed
          agreed    ->  agree
          disabled  ->  disable

          matting   ->  mat
          mating    ->  mate
          meeting   ->  meet
          milling   ->  mill
          messing   ->  mess

          meetings  ->  meet

   */

   private final void step1()
   {  if (b[k] == 's')
      {  if (ends("sses")) k -= 2; else
         if (ends("ies")) setto("i"); else
         if (b[k-1] != 's') k--;
      }
      if (ends("eed")) { if (m() > 0) k--; } else
      if ((ends("ed") || ends("ing")) && vowelinstem())
      {  k = j;
         if (ends("at")) setto("ate"); else
         if (ends("bl")) setto("ble"); else
         if (ends("iz")) setto("ize"); else
         if (doublec(k))
         {  k--;
            {  int ch = b[k];
               if (ch == 'l' || ch == 's' || ch == 'z') k++;
            }
         }
         else if (m() == 1 && cvc(k)) setto("e");
     }
   }

   /* step2() turns terminal y to i when there is another vowel in the stem. */

   private final void step2() { if (ends("y") && vowelinstem()) b[k] = 'i'; }

   /* step3() maps double suffices to single ones. so -ization ( = -ize plus
      -ation) maps to -ize etc. note that the string before the suffix must give
      m() > 0. */

   private final void step3() { if (k == 0) return; /* For Bug 1 */ switch (b[k-1])
   {
       case 'a': if (ends("ational")) { r("ate"); break; }
                 if (ends("tional")) { r("tion"); break; }
                 break;
       case 'c': if (ends("enci")) { r("ence"); break; }
                 if (ends("anci")) { r("ance"); break; }
                 break;
       case 'e': if (ends("izer")) { r("ize"); break; }
                 break;
       case 'l': if (ends("bli")) { r("ble"); break; }
                 if (ends("alli")) { r("al"); break; }
                 if (ends("entli")) { r("ent"); break; }
                 if (ends("eli")) { r("e"); break; }
                 if (ends("ousli")) { r("ous"); break; }
                 break;
       case 'o': if (ends("ization")) { r("ize"); break; }
                 if (ends("ation")) { r("ate"); break; }
                 if (ends("ator")) { r("ate"); break; }
                 break;
       case 's': if (ends("alism")) { r("al"); break; }
                 if (ends("iveness")) { r("ive"); break; }
                 if (ends("fulness")) { r("ful"); break; }
                 if (ends("ousness")) { r("ous"); break; }
                 break;
       case 't': if (ends("aliti")) { r("al"); break; }
                 if (ends("iviti")) { r("ive"); break; }
                 if (ends("biliti")) { r("ble"); break; }
                 break;
       case 'g': if (ends("logi")) { r("log"); break; }
   } }

   /* step4() deals with -ic-, -full, -ness etc. similar strategy to step3. */

   private final void step4() { switch (b[k])
   {
       case 'e': if (ends("icate")) { r("ic"); break; }
                 if (ends("ative")) { r(""); break; }
                 if (ends("alize")) { r("al"); break; }
                 break;
       case 'i': if (ends("iciti")) { r("ic"); break; }
                 break;
       case 'l': if (ends("ical")) { r("ic"); break; }
                 if (ends("ful")) { r(""); break; }
                 break;
       case 's': if (ends("ness")) { r(""); break; }
                 break;
   } }

   /* step5() takes off -ant, -ence etc., in context <c>vcvc<v>. */

   private final void step5()
   {   if (k == 0) return; /* for Bug 1 */ switch (b[k-1])
       {  case 'a': if (ends("al")) break; return;
          case 'c': if (ends("ance")) break;
                    if (ends("ence")) break; return;
          case 'e': if (ends("er")) break; return;
          case 'i': if (ends("ic")) break; return;
          case 'l': if (ends("able")) break;
                    if (ends("ible")) break; return;
          case 'n': if (ends("ant")) break;
                    if (ends("ement")) break;
                    if (ends("ment")) break;
                    /* element etc. not stripped before the m */
                    if (ends("ent")) break; return;
          case 'o': if (ends("ion") && j >= 0 && (b[j] == 's' || b[j] == 't')) break;
                                    /* j >= 0 fixes Bug 2 */
                    if (ends("ou")) break; return;
                    /* takes care of -ous */
          case 's': if (ends("ism")) break; return;
          case 't': if (ends("ate")) break;
                    if (ends("iti")) break; return;
          case 'u': if (ends("ous")) break; return;
          case 'v': if (ends("ive")) break; return;
          case 'z': if (ends("ize")) break; return;
          default: return;
       }
       if (m() > 1) k = j;
   }

   /* step6() removes a final -e if m() > 1. */

   private final void step6()
   {  j = k;
      if (b[k] == 'e')
      {  int a = m();
         if (a > 1 || a == 1 && !cvc(k-1)) k--;
      }
      if (b[k] == 'l' && doublec(k) && m() > 1) k--;
   }

   /** Stem the word placed into the Stemmer buffer through calls to add().
    * Returns true if the stemming process resulted in a word different
    * from the input.  You can retrieve the result with
    * getResultLength()/getResultBuffer() or toString().
    */
   public void stem()
   {  k = i - 1;
      if (k > 1) { step1(); step2(); step3(); step4(); step5(); step6(); }
      i_end = k+1; i = 0;
   }

   /** Test program for demonstrating the Stemmer.  It reads text from a
    * a list of files, stems each word, and writes the result to standard
    * output. Note that the word stemmed is expected to be in lower case:
    * forcing lower case must be done outside the Stemmer class.
    * Usage: Stemmer file-name file-name ...
    */
   public static void main(String[] args)
   {
      char[] w = new char[501];
      Stemmer s = new Stemmer();
      for (int i = 0; i < args.length; i++)
      try
      {
         FileInputStream in = new FileInputStream(args[i]);

         try
         { while(true)

           {  int ch = in.read();
              if (Character.isLetter((char) ch))
              {
                 int j = 0;
                 while(true)
                 {  ch = Character.toLowerCase((char) ch);
                    w[j] = (char) ch;
                    if (j < 500) j++;
                    ch = in.read();
                    if (!Character.isLetter((char) ch))
                    {
                       /* to test add(char ch) */
                       for (int c = 0; c < j; c++) s.add(w[c]);

                       /* or, to test add(char[] w, int j) */
                       /* s.add(w, j); */

                       s.stem();
                       {  String u;

                          /* and now, to test toString() : */
                          u = s.toString();

                          /* to test getResultBuffer(), getResultLength() : */
                          /* u = new String(s.getResultBuffer(), 0, s.getResultLength()); */

                          System.out.print(u);
                       }
                       break;
                    }
                 }
              }
              if (ch < 0) break;
              System.out.print((char)ch);
           }
         }
         catch (IOException e)
         {  System.out.println("error reading " + args[i]);
            break;
         }
      }
      catch (FileNotFoundException e)
      {  System.out.println("file " + args[i] + " not found");
         break;
      }
   }
}






