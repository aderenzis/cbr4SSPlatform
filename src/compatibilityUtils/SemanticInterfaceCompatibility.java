package compatibilityUtils;

import edu.giisco.SoaML.metamodel.*;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class SemanticInterfaceCompatibility {
		
		/**
			 * @param args
			 */
			
			static IDictionary dictionary = null;
//			static String path = "C:\\Program Files (x86)\\WordNet\\2.1\\dict";
//			static String path = java.lang.System.getenv("WNSEARCHDIR");
			static String path = System.getenv("WNHOME")+File.separator+"dict"+File.separator;
			static URL url = null;
			static int MAXV=1000;
			private static double verbExact=0.0;
			private static double PARAM_CASE_PENALIZATION = 0.0;
			private static double ELEMENT_NUMBER_PENALIZATION = 0.0;
	private static Vector<String> vStopWords;

	public static boolean areSynonyms(String word1, String word2) {
				Vector sinonimosWord1 = getSynonyms(word1);
				if (sinonimosWord1.contains(word2) ) {
					return true;
				} else {
					Vector sinonimosWord2 = getSynonyms(word2);
					if (sinonimosWord2.contains(word1)) {
						return true;
					}
				}
				return false;
			}
			
			public static Vector getSynonyms(String wordQuery) {
				Vector resultado = new Vector();
				IIndexWord idxWord = dictionary.getIndexWord(wordQuery, POS.NOUN);
				if (idxWord == null) {
					idxWord = dictionary.getIndexWord(wordQuery, POS.VERB);
				if (idxWord == null) {
						idxWord = dictionary.getIndexWord(wordQuery, POS.ADJECTIVE);
					}
				if (idxWord == null) {
					idxWord = dictionary.getIndexWord(wordQuery, POS.ADVERB);
				}
					
						
					//logger.debug("No existe la palabra: " + wordQuery);
					if (idxWord == null)
						return resultado;
				}
				List<IWordID> words = idxWord.getWordIDs();
				// Cada una de estas parabras (IWord) tienen el mismo Lexema, pero
				// distinto significado
				for (Iterator<IWordID> iterator = words.iterator(); iterator.hasNext();) {
					IWordID wordID = iterator.next();
					IWord word = dictionary.getWord(wordID);
					// Recuperamos el conjunto de sinonimos para el IWord
					ISynset synset = dictionary.getSynset(word.getSynset().getID());
					// Iteramos sobre el conjunto de palabras (IWord) que son
					// sinónimos
					for (IWord sinonimo : synset.getWords()) {
						String lemaSin = sinonimo.getLemma();
						if (!lemaSin.equals(wordQuery) && !resultado.contains(lemaSin)) {
							// agregamos el sinónimo al vector de términos
							resultado.add(lemaSin);
						}
					}
				}
				return resultado;
			}
			
			
			
			public static boolean isHiperonym(String word1, String word2) {
				Vector hiperonimosWord2 = getHyperonyms(word2);
				if (hiperonimosWord2.contains(word1) ) {
					return true;
				/*}*else {
					Vector hiperonimosWord2 = getHyperonyms(word2);
					if (hiperonimosWord2.contains(word1)) {
						return true;
					}*/
				}
				else
					return false;
			}
			
			public static boolean isHiponym(String word1, String word2) {
				Vector hiponimosWord2 = getHyponyms(word2);
				if (hiponimosWord2.contains(word1) ) {
					return true;
				}
				else
					return false;
			}
			
			public static Vector getHyperonyms(String wordQuery) { //Obtiene Hyperonimos de todos los sinonimos de la palabra
				Vector resultado = new Vector();
				IIndexWord idxWord = dictionary.getIndexWord(wordQuery, POS.NOUN);
				
				if (idxWord == null) {
					idxWord = dictionary.getIndexWord(wordQuery, POS.VERB);
				if (idxWord == null) {
						idxWord = dictionary.getIndexWord(wordQuery, POS.ADJECTIVE);
					}
				if (idxWord == null) {
					idxWord = dictionary.getIndexWord(wordQuery, POS.ADVERB);
				}
				if (idxWord == null)
					return resultado;
				}
				List<IWordID> words = idxWord.getWordIDs();		
				for (Iterator<IWordID> iterator = words.iterator(); iterator.hasNext();) {
					IWordID wordID = iterator.next();			
					IWord word = dictionary.getWord(wordID);
					ISynset synset = dictionary.getSynset(word.getSynset().getID());
					
					
					List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);
					List<IWord> wordsList = null;
					for (ISynsetID sid : hypernyms) {				
						wordsList = dictionary.getSynset(sid).getWords();
						for (Iterator<IWord> i = wordsList.iterator(); i.hasNext();) {					
							String lexemaString = i.next().getLemma();
							if(!resultado.contains(lexemaString))
								resultado.add(lexemaString);
						}
					}
				}
				return resultado;
			}
			
			public static Vector getHyponyms(String wordQuery) { //Obtiene Hyponimos de todos los sinonimos de la palabra
				Vector resultado = new Vector();
				IIndexWord idxWord = dictionary.getIndexWord(wordQuery, POS.NOUN);
				
				if (idxWord == null) {
					idxWord = dictionary.getIndexWord(wordQuery, POS.VERB);
				if (idxWord == null) {
						idxWord = dictionary.getIndexWord(wordQuery, POS.ADJECTIVE);
					}
				if (idxWord == null) {
					idxWord = dictionary.getIndexWord(wordQuery, POS.ADVERB);
				}
				if (idxWord == null)
					return resultado;
				}
				List<IWordID> words = idxWord.getWordIDs();		
				for (Iterator<IWordID> iterator = words.iterator(); iterator.hasNext();) {
					IWordID wordID = iterator.next();			
					IWord word = dictionary.getWord(wordID);
					ISynset synset = dictionary.getSynset(word.getSynset().getID());
					
					
					List<ISynsetID> hyponyms = synset.getRelatedSynsets(Pointer.HYPONYM);
					List<IWord> wordsList = null;
					for (ISynsetID sid : hyponyms) {				
						wordsList = dictionary.getSynset(sid).getWords();
						for (Iterator<IWord> i = wordsList.iterator(); i.hasNext();) {					
							String lexemaString = i.next().getLemma();
							if(!resultado.contains(lexemaString))
								resultado.add(lexemaString);
						}
					}
				}
				return resultado;
			}
			
			

			
	

			public static Vector separarTerminosAuxFine(String term)
			{
				Vector vec = new Vector();
				if(term.length()>0)
				{
					boolean mayus =false;
					String ret="";
					String retMayus="";
					char lastMayus=0;
					char charAux;
					if(term.charAt(0)>=65 && term.charAt(0)<=90) // Si es mayuscula la 1er letra
					{
						charAux= (char) (term.charAt(0)+32); // guarda la minuscula
						ret=Character.toString(charAux); // ret almaceno la letra
						retMayus=Character.toString(charAux); // retMayus almaceno
						mayus=true; 
					}
					else
						ret=Character.toString(term.charAt(0)); // si no es mayuscula se almacena el char en ret
					for(int i=1;i< term.length();i++)
					{
						String auxiliar = Character.toString(term.charAt(i));
						if(term.charAt(i)>=65 && term.charAt(i)<=90) // Si es una mayuscula
						{
							//if(ret.length()>0 || retMayus.length()>0)
								
								if(!mayus) //Es la primer Mayuscula
								{
									if(retMayus.length()>1) // Ya existia anteriormente una seguidilla de mayusculas
									{
										if(isWord(lastMayus+ret))//es una palabra la ultima mayuscula + minusculas
										{
											vec.add(retMayus.substring(0, retMayus.length()-1));
											vec.add(lastMayus+ret);
											lastMayus=0;
											retMayus="";
											ret="";
										}
										else
										{
											vec.add(retMayus);
											vec.add(ret);
											lastMayus=0;
											retMayus="";
											ret="";
										}
									}
									else // No existia anteriormente una seguidilla de mayusculas
										if(ret.length()>0)
											vec.add(ret);
									
									mayus=true;
									charAux= (char) (term.charAt(i)+32);
									ret=Character.toString(charAux);
									retMayus=Character.toString(charAux);
								} 
								else //No es la primer mayuscula consecutiva
								{
									charAux= (char) (term.charAt(i)+32);
									retMayus = retMayus+charAux;
									ret="";
								}
							
							
						}
						else //No es una Mayuscula
						{
							if(term.charAt(i) == 45 || term.charAt(i)== 95 || esNumero(term.charAt(i))) //  Si es _ o -
							{
								if(ret.length()>0) // si el guion esta despues de una acumulacion de Minusculas
								{
									vec.add(ret);
									ret="";
									retMayus="";
								}
								else if(retMayus.length()>0) // si el guion esta despues de una acumulacion de Mayusculas
								{
									vec.add(retMayus);
									retMayus="";
								}
									
								mayus=false;
							} // No es mayuscula ni _ ni - ni Numero// es una letra minuscula
							else
							{
								if(mayus) // la Letra anterior era una mayuscula
								{
									lastMayus= (char) (term.charAt(i-1)+32);
									ret=ret+term.charAt(i);
									mayus=false;
								}
								else // la letra anterior no era mayuscula
								{
									ret=ret+term.charAt(i);
								}
								
							}
						}
					}
					if(ret.length()>0 | retMayus.length()>1)
					{
						if(retMayus.length()>1) // Ya existia anteriormente una seguidilla de mayusculas
						{
							if(lastMayus != 0 && ret.length()>0 && isWord(lastMayus+ret)) // Es un && porque si lastMayus es 0 no debe entrar al metodo isWord.
							{
								vec.add(retMayus.substring(0, retMayus.length()-1)); 
								vec.add(lastMayus+ret);
								lastMayus=0;
								retMayus="";
								ret="";
							}
							else
							{
								if(retMayus.length()>1);
									vec.add(retMayus);
								if(ret.length()>0)
									vec.add(ret);
								lastMayus=0;
								retMayus="";
								ret="";
							}
						}
						else
							vec.add(ret);
					}
				}
				return vec;
			}
			
			private static boolean esNumero(char charAt) {
				
				return (48<=charAt && charAt<=57);
			}
			
			public static boolean isWord (String wordQuery)
			{
					IIndexWord idxWord = dictionary.getIndexWord(wordQuery, POS.NOUN);
					if (idxWord == null) 
						idxWord = dictionary.getIndexWord(wordQuery, POS.VERB);
					if (idxWord == null) 
						idxWord = dictionary.getIndexWord(wordQuery, POS.ADJECTIVE);
				    if (idxWord == null) 
						idxWord = dictionary.getIndexWord(wordQuery, POS.ADVERB);
					if (idxWord == null)
						return false;
					else				
						return true;
				
			}
			
			
			
			
						
			public static Vector getVectorSteaming(Vector vec)
			{
				WordnetStemmer wns= new WordnetStemmer(dictionary); 
				int length = vec.size();
				List<String> stems1;
				List<String> steams2;
				Vector ret=new Vector();
				int aux;
				for (int i=0;i<length;i++)
				{
					stems1 = wns.findStems((String) vec.get(i),POS.NOUN);
					if (stems1.size()==0)
						stems1 = wns.findStems((String) vec.get(i),POS.VERB);
					if (stems1.size()==0)
						stems1 = wns.findStems((String) vec.get(i),POS.ADVERB);
					if(stems1.size()==0) // Si la palabra no existe en el diccionario la agrega a la lista de stems. Posible abrebiatura o Sigla
					{
						if(!ret.contains(vec.get(i)))
							ret.add(vec.get(i));
					}
					//stems2 = wns.findStems((String) vec.get(i), POS.ADJECTIVE); //Con Los adjetivos no funciona BIEN.
					for (aux=0;aux<stems1.size();aux++)
						if(!ret.contains(stems1.get(aux)))
							ret.add(stems1.get(aux));
					/*for (aux=0;aux<stems2.size();aux++)
						if(!ret.contains(stems2.get(aux)))
							ret.add(stems2.get(aux));*/
				}
				return ret;
			}
			
			public static int[] assessNameCompatibility(String str1, String str2 ,Vector retVecStems) 
			{
				
				try {
					url = new URL("file", "localhost", path);
				} catch (MalformedURLException e) {
				}
				if (url == null) 
					return null;
				if (dictionary == null)
					dictionary = new Dictionary(url);
				try {
					if (!dictionary.isOpen())
						dictionary.open();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				int[] ret = {0,0,0,0};
				if(str1.length()>0 && str2.length()>0)
				{
					{
						Vector vstr1 = separarTerminosAuxFine(str1);	
						Vector vstr2 = separarTerminosAuxFine(str2);
						Vector vstr1Aux = (Vector) vstr1.clone();			
						Vector vstr2Aux = (Vector)vstr2.clone();
						if(sonExactos(vstr1Aux,vstr2Aux))
						{
							//ret[0]= MAXV;
							ret[0] = cantElement(vstr1Aux, vstr2Aux);
							retVecStems.add(0,vstr1Aux.clone());
							retVecStems.add(1,vstr2Aux.clone());
						}
						else
						{
							try {
								removeStopWords(vstr1,vstr2);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							Vector steams1 = getVectorSteaming(vstr1);
							retVecStems.add(0, steams1.clone());
							Vector steams2 = getVectorSteaming(vstr2);
							retVecStems.add(1, steams2.clone());
							verbExact=0.0;
							ret[0]=cantidadExactos(steams1,steams2);
							ret[1]=cantidadSinonimos(steams1,steams2);
							ret[2]=cantidadHiperonimos(steams1,steams2);
							ret[3]=cantidadHiponimos(steams1,steams2);
							
						}
					}
					
				}
				return ret;
			}
			
		public static int cantidadSinonimos (Vector<String> name1Vector, Vector<String> name2Vector) {
				
				int ret = 0;
				int i = 0;
				String s;
				int j;
				boolean band;
				
				while (i < name1Vector.size()) {
					s= (String) name1Vector.get(i);
					j=0;
					band = true;
					while(j < name2Vector.size()){
						if(areSynonyms(s,(String)name2Vector.get(j)))
						{
							if((i==0 || j==0) && verbExact!=0.0)
								verbExact=0.2;
							band=false;
							ret++;
							name1Vector.remove(i);
							name2Vector.remove(j);
						}
						else
							j++;
					}
					if (band)
						i++;
				}
				return ret;
			}

		public static int cantidadHiperonimos (Vector<String> name1Vector, Vector<String> name2Vector) {
			
			int ret = 0;
			int i = 0;
			String s;
			int j;
			boolean band;
			
			while (i < name1Vector.size()) {
				s= (String) name1Vector.get(i);
				j=0;
				band = true;
				while(j < name2Vector.size()){
					if(isHiperonym(s,(String)name2Vector.get(j))&& band) //Controlar And band [get, access, control, list] [get, total, scheduled, tasks, log, records]
					{
						band=false;
						ret++;
						name1Vector.remove(i);
						name2Vector.remove(j);
					}
					else
						j++;
				}
				if (band)
					i++;
			}
			return ret;
		}

		public static int cantidadHiponimos (Vector<String> name1Vector, Vector<String> name2Vector) {
			
			int ret = 0;
			int i = 0;
			String s;
			int j;
			boolean band;
			
			while (i < name1Vector.size()) {
				s= (String) name1Vector.get(i);
				j=0;
				band = true;
				while(j < name2Vector.size()&&band){ //CONTROLAR AND BAND--   que pasa con [create, portfolio] [list, roles]
					
					if(isHiponym(s,(String)name2Vector.get(j)))
					{
						band=false;
						ret++;
						name1Vector.remove(i);
						name2Vector.remove(j);
					}
					else
						j++;
				}
				if (band)
					i++;
			}
			return ret;
		}

		public static int cantidadExactos (Vector<String> name1Vector, Vector<String> name2Vector) {
			
			int ret = 0;
			int i = 0;
			while (i < name2Vector.size()) {
				if (name1Vector.contains(name2Vector.get(i)))
				{				ret++;
					if(i==0)
						verbExact=0.2;
					name1Vector.remove(name2Vector.get(i));
					name2Vector.remove(i);
				}
				else
					i += 1;
			}
			return ret;
		}
			
			public static void removeStopWords(Vector v1 , Vector v2) throws IOException
			{
				//BufferedReader entrada = new BufferedReader(new FileReader("D:\\TESIS\\TesisDeRenzis\\EvSemServWeb\\src\\stoplist.txt"));
				
//				BufferedReader entrada = new BufferedReader(new FileReader(System.getenv("WNHOME")+"stoplist.txt"));

				if (vStopWords == null || vStopWords.size() == 0) {
					vStopWords = new Vector();
					BufferedReader entrada = new BufferedReader(new FileReader(System.getenv("WNHOME") + File.separator + "stoplist.txt"));
					String renglon;
					while ((renglon = entrada.readLine()) != null) {
						vStopWords.add(renglon);
					}
					entrada.close();
				}

				////////////System.out.println("Cantidad de StopWords: "+vStopWords.size());
				////////////System.out.println(vStopWords);
				v1.removeAll(vStopWords);
				v2.removeAll(vStopWords);

			}
			
			public static boolean sonExactos(Vector<String> vstr1 , Vector<String> vstr2)
			{	
				boolean ret=false;
				int cont=0;
				if(vstr2.size() == vstr1.size())
				{
						////////////System.out.println("Flag 3");
						int i=0;
						boolean band=true;
						while(i<vstr1.size() && band)
						{
							int j =0;
							while(j<vstr2.size()&& band)
							{
								if(vstr1.get(i).equalsIgnoreCase(vstr2.get(j)))
								{
									vstr2.remove(j);
									cont++;	
								}
								else
									j++;
							}
							i++;
						}
						if(vstr1.size()==cont)
							ret=true;
				}
				return ret;
			}
			
						public static double compatibleValue(int[]res , Vector v1, Vector v2)
			{
				return ((res[0]+res[1]+0.5*res[2]+0.5*res[3])/(cantElement(v1,v2)-res[1]));
			}
			
			
			public static int cantElement(Vector v1, Vector v2)
			{
				int ret = v1.size();
				for(int i=0; i<v2.size();i++)
				{
					if(!v1.contains(v2.get(i)))
						ret++;
				}
				return ret;
				
			}
			
			private static double[][] calcularCompatibilidadNombreParametros(ArrayList <Parameter> m1Parameters , ArrayList <Parameter> m2Parameters) throws IOException{
				
				double[][] ret;
				ret = new double [m1Parameters.size()][m2Parameters.size()];
				int i=0;
				int j=0;
				int[] semanticResult;
				Vector vecStems = new Vector();
				for(Parameter m1Parameter: m1Parameters)
				{
					j=0;
					for(Parameter m2Parameter: m2Parameters)
					{
						semanticResult= SemanticInterfaceCompatibility.assessNameCompatibility(m1Parameter.getName(),m2Parameter.getName(), vecStems);
						//////System.out.println("Semantic Result: "+ semanticResult[0]+"-"+semanticResult[1]+"-"+semanticResult[2]+"-"+semanticResult[3]);
						//////System.out.println("Vector 1: "+(Vector) vecStems.get(0));
						//////System.out.println("Vector 2: "+(Vector) vecStems.get(1));
						ret[i][j] = 1+SemanticInterfaceCompatibility.compatibleValue(semanticResult,(Vector) vecStems.get(0), (Vector)vecStems.get(1));
						j++;
					}
					i++;
				}
				
				return ret;
			}
			
			 
			public static double[][] getMatrizCompatibleParameters(List <Parameter> requiredOperationParameters,List <Parameter> candidateOperationParameters){
				int sizeC1 = requiredOperationParameters.size();
				int sizeC2 = candidateOperationParameters.size();
				double[][] ret= new double[sizeC1][sizeC2];
				String typeParM1;
				String typeParM2;
				for(int i =0;i<sizeC1;i++){
					//System.out.println(requiredOperationParameters.get(i).getName());
					typeParM1 = requiredOperationParameters.get(i).getType().getName();
					for(int j=0;j<sizeC2;j++){
						typeParM2 = candidateOperationParameters.get(j).getType().getName();
						if(typeParM1.equalsIgnoreCase(typeParM2)){
							ret[i][j] = 2;
						}
						else{
							try {
								ret[i][j] = InterfacesCompatibilityChecker.
										calculateTypeCompatibility(requiredOperationParameters.get(i).getType(),candidateOperationParameters.get(j).getType());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}						
					}
				}
				return ret;
			}
			
			private static double[][] multiplicarEscalares(double[][] m1,double[][] m2)
			{
				double[][] ret = new double[0][0];
				if(m1.length>0){
					ret = new double[m1.length][m1[0].length];
					for(int i=0;i<m1.length;i++)
					{
						for(int j=0;j<m1[0].length;j++)
						{
							ret[i][j] = m1[i][j]*m2[i][j]; 
						}
					}
				}
				return ret;
			}
			
			public static double paramCaseValue(Operation requiredOperation,Interface requiredInterface, 
					Operation candidateOperation, Interface serviceInterface, Vector<Vector<int[]>> recomendations) throws IOException
			{
				double paramCasePenalization=0;
				if(requiredOperation.getInput().getParameters().size()<candidateOperation.getInput().getParameters().size())
					paramCasePenalization= PARAM_CASE_PENALIZATION*(candidateOperation.getInput().getParameters().size() -requiredOperation.getInput().getParameters().size());
				double[][] matrizNombresParametros;
				double[][] matrizTipoParametros;
				matrizNombresParametros= calcularCompatibilidadNombreParametros(requiredOperation.getInput().getParameters(), candidateOperation.getInput().getParameters());
				matrizTipoParametros = getMatrizCompatibleParameters(requiredOperation.getInput().getParameters(),candidateOperation.getInput().getParameters());
				double[][] matricesMultiplicadas=multiplicarEscalares(matrizNombresParametros, matrizTipoParametros);
				int[][] resultParam = HungarianAlgorithm.hgAlgorithm(matricesMultiplicadas, "max");
				//System.out.println(requiredOperation.getName()+" "+ candidateOperation.getName());
				imprimirMatriz(matrizNombresParametros);
				imprimirMatriz(matrizTipoParametros);
				imprimirMatriz(matricesMultiplicadas);
				imprimirMatriz(resultParam);
				double sum =0;
				for (int j=0; j<resultParam.length; j++)
				{
					//<COMMENT> to avoid printing the elements that make up the assignment
					//System.out.printf("\t array(%d,%d) = %.2f\n", (resultParam[j][0]+1), (resultParam[j][1]+1),
					//matrizNombresParametros[resultParam[j][0]][resultParam[j][1]]);
					sum = sum + matricesMultiplicadas[resultParam[j][0]][resultParam[j][1]];
					//</COMMENT>
				}
				//////System.out.println("\t total:"+ sum);
				double ret = (sum/candidateOperation.getInput().getParameters().size()) - paramCasePenalization;
				
				double valor = 0.0;
				Vector<Integer> combParamOpR = new Vector<Integer>();
				Vector<Integer> combParamOpS = new Vector<Integer>();
				Vector<int[]> significantComb = new Vector<int[]>();
				if(requiredOperation.getInput().getParameters().size()< candidateOperation.getInput().getParameters().size()){
					for(int i=0;i<candidateOperation.getInput().getParameters().size();i++){
						boolean band=false;
						for (int j=0; j<requiredOperation.getInput().getParameters().size(); j++)
						{
							if(resultParam[j][1]==i){
								band=true;
							}
						}
						if(!band){
							combParamOpS.add(i);
						}
					}
				}
				for (int j=0; j<resultParam.length; j++)
				{
					valor = matrizNombresParametros[resultParam[j][0]][resultParam[j][1]];
			//		valorTipo = matrizTipoParametros[resultParam[j][0]][resultParam[j][1]];
					if(valor == 1) //|| valorTipo==1
					{
						combParamOpR.add(resultParam[j][0]);
						combParamOpS.add(resultParam[j][1]);
					} else {
						significantComb.add(resultParam[j]);
					}
				}
				Vector<Vector<int[]>> permutaciones = Permutaciones.getPermutaciones(combParamOpR, combParamOpS);
				//Vector<Vector<int[]>> recomendations = new Vector<Vector<int[]>>();
				if(permutaciones != null && permutaciones.size()>0) {
					for(Vector<int[]> permutacion : permutaciones) {
						significantComb.addAll(permutacion);
						recomendations.add((Vector<int[]>)significantComb.clone());
						significantComb.removeAll(permutacion);
					}
				}
				else{
					recomendations.add((Vector<int[]>) significantComb.clone());
				}
				
//				for(Integer paramOpR:combParamOpR){
//					for(Integer paramOpS:combParamOpS){
//						Vector<int[]> recomendation = new Vector<int[]>();
//						recomendation.addAll(significantComb);
//					}
//				}
				recomendations.clone();
				return ret;
			}
			
			public static double returnCaseValue(Operation requiredOperation,Interface requiredInterface, 
					Operation candidateOperation, Interface serviceInterface, Vector<Vector<int[]>> recomendations) throws IOException
			{
				double paramCasePenalization=0;
				if(requiredOperation.getOutput().getParameters().size()<candidateOperation.getOutput().getParameters().size())
					paramCasePenalization= PARAM_CASE_PENALIZATION*(candidateOperation.getOutput().getParameters().size() -requiredOperation.getOutput().getParameters().size());
				double[][] matrizNombresParametros;
				double[][] matrizTipoParametros;
				matrizNombresParametros= calcularCompatibilidadNombreParametros(requiredOperation.getOutput().getParameters(), candidateOperation.getOutput().getParameters());
				matrizTipoParametros = getMatrizCompatibleParameters(requiredOperation.getOutput().getParameters(),candidateOperation.getOutput().getParameters());
				imprimirMatriz(matrizNombresParametros);
				//System.out.println("---------------------------------");
				imprimirMatriz(matrizTipoParametros);
				double[][] matricesMultiplicadas=multiplicarEscalares(matrizNombresParametros, matrizTipoParametros);

				int[][] resultParam = HungarianAlgorithm.hgAlgorithm(matricesMultiplicadas, "max");
				//System.out.println(requiredOperation.getName()+" "+ candidateOperation.getName());
				imprimirMatriz(matrizNombresParametros);
				imprimirMatriz(matrizTipoParametros);
				imprimirMatriz(matricesMultiplicadas);
				imprimirMatriz(resultParam);
				double sum =0;
				for (int j=0; j<resultParam.length; j++)
				{
					//<COMMENT> to avoid printing the elements that make up the assignment
					//System.out.printf("\t array(%d,%d) = %.2f\n", (resultParam[j][0]+1), (resultParam[j][1]+1),
					//matrizNombresParametros[resultParam[j][0]][resultParam[j][1]]);
					sum = sum + matricesMultiplicadas[resultParam[j][0]][resultParam[j][1]];
					//</COMMENT>
				}
				//////System.out.println("\t total:"+ sum);
				double ret = (sum/candidateOperation.getOutput().getParameters().size()) - paramCasePenalization;
				
				double valor = 0.0;
				Vector<Integer> combParamOpR = new Vector<Integer>();
				Vector<Integer> combParamOpS = new Vector<Integer>();
				Vector<int[]> significantComb = new Vector<int[]>();
				if(requiredOperation.getOutput().getParameters().size()< candidateOperation.getOutput().getParameters().size()){
					for(int i=0;i<candidateOperation.getOutput().getParameters().size();i++){
						boolean band=false;
						for (int j=0; j<requiredOperation.getOutput().getParameters().size(); j++)
						{
							if(resultParam[j][1]==i){
								band=true;
							}
						}
						if(!band){
							combParamOpS.add(i);
						}
					}
				}
				for (int j=0; j<resultParam.length; j++)
				{
					valor = matrizNombresParametros[resultParam[j][0]][resultParam[j][1]];
			//		valorTipo = matrizTipoParametros[resultParam[j][0]][resultParam[j][1]];
					if(valor == 1) //|| valorTipo==1
					{
						combParamOpR.add(resultParam[j][0]);
						combParamOpS.add(resultParam[j][1]);
					} else {
						significantComb.add(resultParam[j]);
					}
				}
				Vector<Vector<int[]>> permutaciones = Permutaciones.getPermutaciones(combParamOpR, combParamOpS);
				//Vector<Vector<int[]>> recomendations = new Vector<Vector<int[]>>();
				if(permutaciones != null && permutaciones.size()>0) {
					for(Vector<int[]> permutacion : permutaciones) {
						significantComb.addAll(permutacion);
						recomendations.add((Vector<int[]>)significantComb.clone());
						significantComb.removeAll(permutacion);
					}
				}
				else{
					recomendations.add((Vector<int[]>) significantComb.clone());
				}
				
//				for(Integer paramOpR:combParamOpR){
//					for(Integer paramOpS:combParamOpS){
//						Vector<int[]> recomendation = new Vector<int[]>();
//						recomendation.addAll(significantComb);
//					}
//				}
				recomendations.clone();
				return ret;
			}
			
			private static void imprimirMatriz(int[][] matricesMultiplicadas) {
				for(int i=0; i<matricesMultiplicadas.length ; i++){
					for(int j=0; j<matricesMultiplicadas[i].length ; j++){
						System.out.print(matricesMultiplicadas[i][j]+"  ");
					}
					//System.out.println();
				}
				
			}

			private static void imprimirMatriz(double[][] matricesMultiplicadas) {
				for(int i=0; i<matricesMultiplicadas.length ; i++){
					for(int j=0; j<matricesMultiplicadas[i].length ; j++){
						System.out.print(matricesMultiplicadas[i][j]+"  ");
					}
					//System.out.println();
				}
				
			}

//			public static double fieldCaseValue(Operation requiredOperation, Operation candidateOperation) 
//			{	double elementCasePenalization=0;
//				Type returnTypeRO= requiredOperation.getResponse().getType();
//				Type returnTypeCO= candidateOperation.getResponse().getType();
//				if (returnTypeRO.getElementsNumber()<=returnTypeCO.getElementsNumber()){
//
//					elementCasePenalization= ELEMENT_NUMBER_PENALIZATION*(returnTypeCO.getElementsNumber() - returnTypeRO.getElementsNumber());
//					
//					double[][] matrizNombreCampos;
//					double[][] matrizTipoCampos;
//					//calcularemos compatibildad de nombres de todos los elements del tipo complejo
//					matrizNombreCampos= calcularCompatibilidadNombreCampos(returnTypeRO.getElements(),returnTypeCO.getElements());
//					//calcularemos compatibildad de nombres de tipo de dato de todos los elements del tipo complejo
//					matrizTipoCampos = getMatrizCompatibleFields(returnTypeRO.getElements(), returnTypeCO.getElements());
//					double[][] matricesMultiplicadas=multiplicarEscalares(matrizNombreCampos, matrizTipoCampos);
//					//obtengo la mejor asignaci�n entre las combinaciones
//					int[][] resultCampos = HungarianAlgorithm.hgAlgorithm(matricesMultiplicadas, "max");
//									
//					//Recorre la matriz resultCampos para ver qu�est�devolviendo
//					/*
//					for (int j=0; j<resultCampos.length; j++)
//					{
//						System.out.printf("Array(%d,%d) = %.2f\n", (resultCampos[j][0]+1), (resultCampos[j][1]+1),
//						matrizNombreCampos[resultCampos[j][0]][resultCampos[j][1]]);
//					}
//					*/
//					double sum =0;
//					for (int j=0; j<resultCampos.length; j++)
//						sum = sum + matricesMultiplicadas[resultCampos[j][0]][resultCampos[j][1]];
//	
//					double ret = (sum/returnTypeRO.getElementsNumber()) - elementCasePenalization;
//					////System.out.println(ret);
//					return ret;
//				}
//				else return 0;
//				
//			}
		

			private static double[][] calcularCompatibilidadNombreCampos (ArrayList<Attribute> atributesA , ArrayList<Attribute> attributesB) 
			{	double[][] ret;
				int sizeElementsA = atributesA.size();
				int sizeElementsB = attributesB.size();
				ret = new double [sizeElementsA][sizeElementsB];
				int[] semanticResult;
				Vector<Vector>vecStems=new Vector <Vector> ();
				for(int i = 0; i < sizeElementsA; i++) 
				{
					for(int j = 0; j < sizeElementsB; j++)
					{				
					//creo arreglo con cantidad de sinonimos,hiponimos, hyperonimos e identicos	
					semanticResult= SemanticInterfaceCompatibility.assessNameCompatibility(atributesA.get(i).getName(),attributesB.get(j).getName() ,vecStems);
					//que tan parecido son los nombres : un numero que va entre 0 y 1
					ret[i][j] = SemanticInterfaceCompatibility.compatibleValue(semanticResult,(Vector) vecStems.get(0), (Vector)vecStems.get(1)); //CONSULTAR EL COMPORTAMIENTO DE LOS CAMPOS DE vectStems
					}
				}
				return ret;
			}
			
			public static double[][] getMatrizCompatibleFields(ArrayList<Attribute> elementsA , ArrayList<Attribute> elementsB)
			{	int sizeA = elementsA.size();
				int sizeB = elementsB.size();
				double[][] ret= new double[sizeA][sizeB];
				String typeFieldM1;
				String typeFieldM2;
				for(int i =0;i<sizeA;i++){
					typeFieldM1 = elementsA.get(i).getType().getName();
					for(int j=0;j<sizeB;j++)
					{
						typeFieldM2 = elementsB.get(j).getType().getName();
						if(typeFieldM1.equalsIgnoreCase(typeFieldM2)){
							ret[i][j] = 2;
						}
						else{							
							if(InterfacesCompatibilityChecker.isSubTyping(typeFieldM1,typeFieldM2)){
								ret[i][j]=1.5;
							}
							else{
								ret[i][j]=1;
							}
						}						
					}
				}
				return ret;
			}
			
			//cuando los dos tipos son complejos
			public static double fieldCaseValueParam(Type requiredType, Type candidateType) throws IOException
			{	
				double fieldCasePenalization=0;
				ArrayList<Attribute> elementsC1Param= requiredType.getElements();
				ArrayList<Attribute> elementsC2Param= candidateType.getElements();
				int elementsC1ParamSize,elementsC2ParamSize;
				System.out.println("Nombre del tipo de atributo:"+ requiredType.getName());
				System.out.println("Nombre del tipo de atributo:"+ candidateType.getName());
				System.out.println("Elementos:"+ requiredType.getElements());
				System.out.println("Elementos:"+ candidateType.getElements());
				elementsC1ParamSize=elementsC1Param.size();
				elementsC2ParamSize=elementsC2Param.size();
				if (elementsC1ParamSize <= elementsC2ParamSize){
					fieldCasePenalization= ELEMENT_NUMBER_PENALIZATION*(elementsC2ParamSize - elementsC1ParamSize);
					double[][] matrizNombreCampos, matrizTipoCampos;
					matrizNombreCampos= calcularCompatibilidadNombreCampos(elementsC1Param,elementsC2Param); 
					matrizTipoCampos = getMatrizCompatibleFields(elementsC1Param, elementsC2Param); 
					double[][] matricesMultiplicadas=multiplicarEscalares(matrizNombreCampos, matrizTipoCampos);
					int[][] resultCampos = HungarianAlgorithm.hgAlgorithm(matricesMultiplicadas, "max");
									
					double sum =0;
					for (int j=0; j<resultCampos.length; j++)
						sum = sum + matricesMultiplicadas[resultCampos[j][0]][resultCampos[j][1]];
					
					double ret = (sum/elementsC1Param.size()) - fieldCasePenalization;
					//System.out.println("ret fieldCaseValueParam = " + ret);
					return ret;
				}
				else 
				{//System.out.println("ret fieldCaseValueParam = 0 (Longitud de Param1 > Param2)"); 
					
				return 0;}
				
			}

}
				
		