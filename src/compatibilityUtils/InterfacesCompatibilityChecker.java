package compatibilityUtils;

import edu.giisco.SoaML.metamodel.*;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author andres
 */
public class InterfacesCompatibilityChecker implements Runnable {
	protected final int NO_COMPATIBLE = 0;
	protected String summary = ""; // @jve:decl-index=0:
	protected String summaryList = ""; // @jve:decl-index=0:

	// La idea es cambiar lo que ahora es TClass por la clase Interface del
	// metamodelo
	Interface requiredInterface; // original class
	Interface serviceInterface; // candidate class


	protected Hashtable<String, Vector> compatibilities; // @jve:decl-index=0:
	private Hashtable<MaximalInterfaceCompatibility, Vector<ParameterCompatibilityPair>> parameterMatching;// recomendacion
																											// unica
																											// de
																											// alan
	private Hashtable<MaximalInterfaceCompatibility, Vector<Vector<int[]>>> parameterMatchingRecomendations;// conjunto
																											// de
																											// recomendaciones
																											// diego
	protected String classPathOriginal;
	protected String classPathCandidate;
	protected int cantidadWrappers;

	public double adaptabilityGap = 0;

	// public InterfacesCompatibilityChecker(JFCompatibility parentWindow,
	// String originalClassName, String candidateClassName,
	// boolean considerInheritedOperations, String classPathOriginal,
	// String classPathCandidate) throws Exception {
	public InterfacesCompatibilityChecker() throws Exception {

		this.parameterMatching = new Hashtable();
		this.parameterMatchingRecomendations = new Hashtable();
		// this.parentWindow = parentWindow;

		// La idea es cambiar lo que ahora es TClass por la clase Interface del
		// metamodelo . VER ARRIBA

		// En principio inicializaremos a mano por eso se comenta la parte que
		// lee.
		// requiredInterface = TestoojClassLoader.load(originalClassName,
		// classPathOriginal,
		// considerInheritedOperations);
		// serviceInterface = TestoojClassLoader.load(candidateClassName,
		// classPathCandidate,
		// considerInheritedOperations);
		// this.classPathOriginal = classPathOriginal;
		// this.classPathCandidate = classPathCandidate;
		// this.parentWindow.setNumberOfIterations(requiredInterface.getOperations().size());
	}

	public Interface getRequiredInterface() {
		return requiredInterface;
	}

	public void setRequiredInterface(Interface requiredInterface) {
		this.requiredInterface = requiredInterface;
	}

	public Interface getServiceInterface() {
		return serviceInterface;
	}

	public void setServiceInterface(Interface serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	public void run() {
		try {
			calculateCompatibilities();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// this.parentWindow.log(this.getSummary());
	}

	protected void calculateCompatibilities() throws IOException {
		// obtengo arraylist de operaciones
		ArrayList<Operation> operations = requiredInterface.getOperations();
		this.compatibilities = new Hashtable<String, Vector>();
		for (int i = 0; i < operations.size(); i++) {
			// this.parentWindow.setCurrentIteration((i + 1));
			// obtengo una operacion
			Operation operation = (Operation) operations.get(i);
			this.compatibilities.put(operation.getName(), new Vector());
			// compara una operacion contra todas las operaciones existentes de
			// serviceInterface
			loadCompatibleMethods(i, requiredInterface, operation, serviceInterface);
		}
		/*
		 * ////System.out.println("INICIO - HASTABLES");
		 * MuestraElementosHash(this.compatibilities); ////System.out.println(
		 * "FIN - HASTABLES");
		 */
		/*
		 * Comentado por lucas, no nos sirve por ahora este metodo
		 * buildSummary();
		 */
		// buildSummaryList();
		// buildSummaryTable();
	}

	/*
	 * protected void buildSummaryTable() { Vector headers = new Vector();
	 * headers.add(""); for (int i = 0; i <
	 * this.serviceInterface.getMethods().size(); i++)
	 * headers.add(this.serviceInterface.getMethod(i).getWholeSignature());
	 * Vector rows = new Vector(); for (int i = 0; i <
	 * this.requiredInterface.getMethods().size(); i++) { Vector row = new
	 * Vector(); for (int j = 0; j < headers.size(); j++) row.add("");
	 * row.setElementAt(this.requiredInterface.getMethod(i).getWholeSignature(),
	 * 0); rows.add(row); } for (int i = 0; i <
	 * this.requiredInterface.getMethods().size(); i++) { // String
	 * aMethod=a.getMethod(i).getWholeSignature(); Operation aMethod =
	 * requiredInterface.getMethod(i); Vector compatibilityRow = (Vector)
	 * this.compatibilities.get(aMethod .getWholeSignature()); for (int j = 0; j
	 * < compatibilityRow.size(); j++) { String bMethod =
	 * compatibilityRow.get(j).toString(); Vector row = (Vector) rows.get(i);
	 * for (int k = 0; k < headers.size(); k++) { String auxi =
	 * headers.get(k).toString(); if (auxi.equals(bMethod)) {
	 * row.setElementAt("+", k); k = headers.size(); } } } }
	 * NonEditableDefaultTableModel tableModel = new
	 * NonEditableDefaultTableModel( rows, headers);
	 * this.parentWindow.setEquivalenceTable(tableModel); }
	 */
	/*
	 * protected void buildSummary() { Enumeration vectores =
	 * this.compatibilities.elements(); int max = 0; while
	 * (vectores.hasMoreElements()) { Vector v = (Vector)
	 * vectores.nextElement(); if (v.size() > max) max = v.size(); }
	 * 
	 * Enumeration<String> keys = this.compatibilities.keys(); StringBuffer sb =
	 * new StringBuffer("<table border=1 FONTSIZE='3'>"); sb.append(
	 * "<tr><th><FONT SIZE='3'>"); sb.append(requiredInterface.getName());
	 * sb.append("</FONT></th>"); sb.append("<th colspan="); sb.append(max);
	 * sb.append("><FONT SIZE='3'>"); sb.append(serviceInterface.getName());
	 * sb.append("</FONT></th></tr>"); while (keys.hasMoreElements()) { String
	 * methodSignature = keys.nextElement(); Vector compatibles = (Vector)
	 * compatibilities.get(methodSignature); // Forma 1era celda de fila - el
	 * metodo original de a if (!compatibles.isEmpty()) sb.append(
	 * "<tr><td bgcolor=\"#F0F8FF\"><FONT SIZE='3'>"); // Hay
	 * compatibles->celeste else sb.append(
	 * "<tr><td bgcolor=\"#FF6347\"><FONT SIZE='3'>");// No Hay compatibles ->
	 * rojo sb.append(methodSignature); sb.append("</FONT></td>");
	 * 
	 * // Forma el resto de la fila - los compatibles de b for (int i = 0; i <
	 * compatibles.size(); i++) { Vector compToPrint = (Vector) ((Vector)
	 * compatibles.get(i)) .clone(); Operation bMethod = (Operation)
	 * compToPrint.get(2); compToPrint.remove(2); compToPrint.add(2,
	 * bMethod.getWholeSignature()); //System.out.println("OO  "
	 * +compToPrint.get(0)); if (compToPrint.get(0).equals(1)) // si es "exact"
	 * Nro caso 1 sb.append("<td bgcolor=\"#F0F8FF\"><FONT SIZE='3'>");// Hay
	 * iguales -> celeste else sb.append("<td><FONT SIZE='3'>");// El resto ->
	 * blanco sb.append(compToPrint.toString()); sb.append("</FONT></td>"); }
	 * sb.append("<td>");
	 * 
	 * for (int i = 0; i < max - compatibles.size(); i++)
	 * sb.append("<td></td>");
	 * 
	 * //Matching parametros sb.append("<td>"); String match = ""; Vector v =
	 * parameterMatching.get(new MaximalInterfaceCompatibility(methodSignature,
	 * "")); if(v!=null && v.size()>0) sb.append(v.toString());
	 * sb.append("</td>"); //fin Matching parametros sb.append("</tr>"); }
	 * sb.append("</table>"); summary = sb.toString(); }
	 * 
	 * protected void buildSummaryList() { // lo estoy haciendo yo Andres.. para
	 * // mostrar un resumen Enumeration vectores =
	 * this.compatibilities.elements(); int max = 0; while
	 * (vectores.hasMoreElements()) { Vector v = (Vector)
	 * vectores.nextElement(); if (v.size() > max) max = v.size(); }
	 * 
	 * Enumeration<String> keys = this.compatibilities.keys(); StringBuffer sb =
	 * new StringBuffer("<table border=0 FONTSIZE='3'>"); sb.append(
	 * "<tr><th><FONT SIZE='3'>"); sb.append(requiredInterface.getName());
	 * sb.append("</FONT></th>"); sb.append("<th colspan= '3'>"); sb.append(
	 * "<FONT SIZE='3'>"); sb.append(serviceInterface.getName());
	 * sb.append("</FONT></th></tr>"); while (keys.hasMoreElements()) { String
	 * signature = keys.nextElement().toString(); Vector compatibles = (Vector)
	 * compatibilities.get(signature);
	 * 
	 * sb.append("<tr><td><FONT SIZE='3'>"); sb.append(signature);
	 * sb.append("</FONT></td>"); if
	 * (compatibles.firstElement().equals(signature)) { // OJO QUE HAY // ERROR
	 * !!!!! sb.append("<td><FONT SIZE='3'>"); sb.append("Yes");
	 * sb.append("</FONT></td>"); // Hay 1 igual sb.append("<td><FONT SIZE='3'>"
	 * ); sb.append(compatibles.size() - 1); sb.append("</FONT></td>"); // El
	 * resto sb.append("<td><FONT SIZE='3'>"); sb.append(compatibles.size());
	 * sb.append("</FONT></td>"); // Total } else { sb.append(
	 * "<td><FONT SIZE='3'>"); sb.append("No"); sb.append("</FONT></td>"); // No
	 * hay 1 igual sb.append("<td><FONT SIZE='3'>");
	 * sb.append(compatibles.size()); sb.append("</FONT></td>"); // Hay 1 igual
	 * sb.append("<td><FONT SIZE='3'>"); sb.append(compatibles.size());
	 * sb.append("</FONT></td>"); // Total } int i = 0; for (; i < max; i++)
	 * sb.append("<td></td>");
	 * 
	 * sb.append("</tr>");
	 * 
	 * } sb.append("</table>"); summaryList = sb.toString(); }
	 */
	//El ADaptabilityGap Varia desde 0 a 1 siendo 0 el mejor caso. 
	private void loadCompatibleMethods(int requiredOperationIndex, Interface requiredInterface,
			Operation requiredOperation, Interface serviceInterface) throws IOException {
		Vector compatibles = (Vector) compatibilities.get(requiredOperation.getName());
		int minValue = 999;
		double minAdaptabilityGap = 8;
		Double maxSim = 0.0;
		MaximalInterfaceCompatibility maximalInterfaceCompatibility = null;
		Vector<ParameterCompatibilityPair> vecParamCompatibilityPair = null;
		Vector<Vector<int[]>> recomendations = null;
		Vector<Vector<int[]>> outPutRecomendations = null;

		for (int i = 0; i < serviceInterface.getOperations().size(); i++) {
			int paramCase = 4; // no compatible
			int nameCase = 4; // distinct names
			int returnCase = 4; // no compatible
			int excepCase = 3; // no compatible
			double adaptabilityGap = 10;
			double parameterResult = 0;
//			agarro una operacion 
			Operation candidateOperation = serviceInterface.getOperations().get(i);

			//System.out.println(requiredOperation.getName() + "  " + candidateOperation.getName());
			recomendations = new Vector<Vector<int[]>>();// tendra el cjto de recomendaciones
			if (requiredOperation.getInput() != null && candidateOperation.getInput() != null
					&& requiredOperation.getInput().getParameters().size() <= candidateOperation.getInput().getParameters().size()
					&& requiredOperation.getInput().getParameters().size() >= ((candidateOperation.getInput().getParameters().size()) / 2)) {
//				if (requiredOperation.getInput().getParameters().size() != 0) {
					
					parameterResult = SemanticInterfaceCompatibility.paramCaseValue(requiredOperation,
							requiredInterface, candidateOperation, serviceInterface, recomendations);
					if (parameterResult >= 4)
						paramCase = 1;
					else if (2 <= parameterResult && parameterResult < 4)
						paramCase = 2;
					else if (0.2 <= parameterResult && parameterResult < 2)
						paramCase = 3;
					else
						paramCase = 4; // No hay incompatibilidad (como minimo es 4)

					adaptabilityGap -= parameterResult;
					//System.out.println("parameterResult: "+parameterResult);
//				}else if (candidateOperation.getInput().getParameters().size() == 0) {
//					adaptabilityGap = -2;
//					paramCase = 1; // No tiene parametros
//				} else
//					paramCase = 4; // En este caso es incompatible porque requiredOperation tiene parametros y candidateOperation no.
			}
			else if (requiredOperation.getInput() == null && candidateOperation.getInput() == null)
			{
				adaptabilityGap = adaptabilityGap-4;
				paramCase = 1; // No tiene parametros
			}
			else
				paramCase = 4; // En este caso es incompatible porque uno tiene parametros y el otro no.

			if (paramCase != 0) { // Si param distinto // NoCompatible => sigue
				// Evaluar Tipo de Resultado
				double returnRes = 0;
				if (requiredOperation.getOutput() != null && candidateOperation.getOutput() != null
						&&requiredOperation.getOutput().getParameters().size() <= candidateOperation.getOutput().getParameters().size()
						&& requiredOperation.getOutput().getParameters().size() >= ((candidateOperation.getOutput().getParameters().size()) / 2)) {
					if (requiredOperation.getOutput().getParameters().size() != 0) {
						outPutRecomendations = new Vector<Vector<int[]>>();// tendra// el// cjto// de// recomendaciones
						returnRes = SemanticInterfaceCompatibility.returnCaseValue(requiredOperation, requiredInterface,
								candidateOperation, serviceInterface, outPutRecomendations);

						if (returnRes >= 4)
							returnCase = 1;
						else if (2 <= returnRes && returnRes < 4)
							returnCase = 2;
						else if (0.2 <= returnRes && returnRes < 2)
							returnCase = 3;
						else
							returnCase = 4; // No hay incompatibilidad (como
											// minimo es 4)

						adaptabilityGap -= returnRes;
					}
					else if (requiredOperation.getOutput() == null && candidateOperation.getOutput() == null)
					{
						adaptabilityGap = adaptabilityGap-4;
						paramCase = 1; // No tiene parametros
					}
					else
							paramCase = 4; // En este caso es incompatible porque uno tiene parametros y el otro no.
//					} else if (candidateOperation.getOutput().getParameters().size() == 0) {
//						adaptabilityGap = -2;
//						returnCase = 1; // No tiene parametros
//					} else
//						returnCase = 4; // En este caso es incompatible porque
//										// c1Opeartion tiene parametros y
//										// candidateOperation no.

				} else
					returnCase = 4;

				if (returnCase != 0) { // Si returnType distinto
										// NoCompatible => sigue
					excepCase = 1; // Se asigna excepCase = 1 para que siga
									// comparando (excepciones no consideradas
									// por ahora)
//					adaptabilityGap -= 2;

					if (excepCase != 0) { // Si Excepciones distinto
											// NoCompatible => sigue
						// Evaluar Nombre de Metodo
						//// //System.out.println("Se compara semanticamente: "+
						// c1Method.getNombre()+ " "+ c2Method.getNombre());
						int[] semanticRes = { 0, 0, 0, 0 };
						Vector<Vector> termVec = new Vector();
						try {
							semanticRes = SemanticInterfaceCompatibility.assessNameCompatibility(
									requiredOperation.getName(), candidateOperation.getName(), termVec);
						} catch (Exception e) {
							e.printStackTrace();
						}
						Double nameResult = SemanticInterfaceCompatibility.compatibleValue(semanticRes, termVec.get(0),
								termVec.get(1));
						if (nameResult >= 1)
							nameCase = 1;
						else if (nameResult >= 0.5)
							nameCase = 2; // condicion N2
						else if (nameResult >= 0.2)
							nameCase = 3;
						else
							nameCase = 4; // Se asigna como minimo N4 para que
											// no se generen incompatibilidades

						adaptabilityGap -= 2 * nameResult;
					}
				}
			}
			//normalizaci�n de adaptabilityGap
			adaptabilityGap=adaptabilityGap/10;
			
//			//System.out.println("returnCase"+returnCase);
//			////System.out.println("nameCase"+nameCase);
//			//System.out.println("paramCase"+paramCase);
//			//System.out.println("excepCase"+excepCase);

			Vector level = getLevelCompatibilidad(returnCase, nameCase, paramCase, excepCase);
			// Establece el nivel de compatibilidad de Interfaces (syntactica)
			// alcanzado
			Integer nroLevel = (Integer) level.get(0);

			/*
			 * if (minAdaptabilityGap != 8){ if( (adaptabilityGap <
			 * minAdaptabilityGap) && c1Method.getParametros().size()>0 &&
			 * c2Method.getParametros().size()>0) {
			 * maximalInterfaceCompatibility = new
			 * MaximalInterfaceCompatibility(c1Method.getWholeSignature(),
			 * c2Method.getWholeSignature()); vecParamCompatibilityPair =
			 * createParamCompatibilityPair(c1Method.getParametros(),
			 * c2Method.getParametros(),recomendations);
			 * this.parameterMatchingRecomendations.put(
			 * maximalInterfaceCompatibility, recomendations);
			 * minAdaptabilityGap = adaptabilityGap; } compatibles =
			 * addCompatibleMethod(compatibles, c2Method, level,
			 * minAdaptabilityGap); } else
			 */ if (adaptabilityGap != 1) {
				// Este es el 1ero compatible que se encuentra
				if (requiredOperation.getInput()!=null&&candidateOperation.getInput()!=null&&
						requiredOperation.getInput().getParameters().size() > 0
						&& candidateOperation.getInput().getParameters().size() > 0
						&& requiredOperation.getInput().getParameters().size() <= candidateOperation.getInput().getParameters().size()
						&& requiredOperation.getInput().getParameters().size() >= ((candidateOperation.getInput().getParameters().size()) / 2)) {
					maximalInterfaceCompatibility = new MaximalInterfaceCompatibility(
							requiredOperation.getWholeSignature(), candidateOperation.getWholeSignature());
					vecParamCompatibilityPair = createParamCompatibilityPair(
							requiredOperation.getInput().getParameters(), candidateOperation.getInput().getParameters(),
							recomendations);
					this.parameterMatchingRecomendations.put(maximalInterfaceCompatibility, recomendations);
					minValue = nameCase + paramCase + excepCase + returnCase;
				}

				minAdaptabilityGap = adaptabilityGap;
				compatibles = addCompatibleMethod(compatibles, candidateOperation, level, minAdaptabilityGap);
//				//System.out.println("\n****************************************************************");
//				//System.out.println(requiredOperation.getName() + "  " + candidateOperation.getName());
//				//System.out.println("minAdaptabilityGap "+minAdaptabilityGap);
//				//System.out.println("****************************************************************\n");
			}
			// this.parameterMatchingRecomendations.put(maximalInterfaceCompatibility,
			// recomendations);
		}

		// compatibles = getSortedVector(compatibles, 108);
		if (maximalInterfaceCompatibility != null && vecParamCompatibilityPair != null) {
			parameterMatching.put(maximalInterfaceCompatibility, vecParamCompatibilityPair);
			/*
			 * aca tengo todas las recomendaciones para esa sola operacion en
			 * cuestion. es el resultado que ira como valor del par clave
			 * (interfaceOriginal(),interfaceService()) de el nuevo hash para
			 * mantener todas las recomendaciones...
			 */

			// this.parameterMatchingRecomendations.put(maximalInterfaceCompatibility,
			// recomendations);
		}
		compatibles = getSortedVector(compatibles, 138);// 137
		compatibilities.put(requiredOperation.getName(), compatibles);
		Vector<ParameterCompatibilityPair> vec = this.parameterMatching
				.get(new MaximalInterfaceCompatibility(requiredOperation.getWholeSignature(), ""));

	}

	private Vector<ParameterCompatibilityPair> createParamCompatibilityPair(ArrayList<Parameter> c1MethodParameters,
			ArrayList<Parameter> c2MethodParameters, Vector<Vector<int[]>> vecResultParam) {

		Vector<ParameterCompatibilityPair> ret = new Vector();
		//System.out.println(" **** " + vecResultParam.size());
		ParameterCompatibilityPair aux;
		Vector<int[]> resultParam = vecResultParam.get(0);
		if (resultParam != null) {
			int a = resultParam.size();
			for (int i = 0; i < resultParam.size(); i++) {
				aux = new ParameterCompatibilityPair(c1MethodParameters.get(resultParam.get(i)[0]).getName(),
						c1MethodParameters.get(resultParam.get(i)[0]).getType().getName(),
						c2MethodParameters.get(resultParam.get(i)[1]).getName(),
						c2MethodParameters.get(resultParam.get(i)[1]).getType().getName());
				ret.add(aux);
			}
		}
		return ret;
	}

	/*
	 * private String nombresYTiposDeParametros(Operation c1Method) {
	 * 
	 * String ret=""; Vector<TParameter> parameters = c1Method.getParametros();
	 * for(TParameter parameter: parameters) { if(ret.length()>0) ret = ret
	 * +"-"+parameter.getMNombre()+":"+parameter.getTipo(); else
	 * ret=parameter.getMNombre()+":"+parameter.getTipo(); } if (ret.equals(""))
	 * ret="SIN PARAMETROS"; return ret; }
	 */

	private Vector getLevelCompatibilidad(int returnCase, int nameCase, int paramCase, int excepCase) {
		// Idenfica el caso de compatibilidad
		Vector level = new Vector();
		if (paramCase != 0 && returnCase != 0 && excepCase != 0 && nameCase != 0) {
			String rCase = "R" + returnCase;
			String nCase = "N" + nameCase;
			String pCase = "P" + paramCase;
			String eCase = "E" + excepCase;
			level.add(rCase);
			level.add(nCase);
			level.add(pCase);
			level.add(eCase);

			// 54 Casos nuevos y autom�ticos, chequeados. R puede ser 1 � 2.
			// N,
			// P, E pueden ser 1,2 � 3.

			if (returnCase == 1 && nameCase == 1 && paramCase == 1 && excepCase == 1) {
				level.add(0, 1); // "exact" - Caso 1
				level.add(1, "exact");
				return level;
			}

			// --[ N_Exact
			// ]-----------------------------------------------------------------------

			if (returnCase == 1 && nameCase == 1 && paramCase == 1 && excepCase == 2) {
				level.add(0, 2); // "n_exact_1" - Caso 2
				level.add(1, "n_exact_1");
				return level;
			}

			if (returnCase == 3 && nameCase == 1 && paramCase == 1 && excepCase == 1) {
				level.add(0, 15); // "n_exact_14" - Caso 15
				level.add(1, "n_exact_14");
				return level;
			}

			if (returnCase == 1 && nameCase == 2 && paramCase == 1 && excepCase == 1) {
				level.add(0, 3); // "n_exact_2" - Caso 3
				level.add(1, "n_exact_2");
				return level;
			}
			if (returnCase == 1 && nameCase == 1 && paramCase == 2 && excepCase == 1) {
				level.add(0, 4); // "n_exact_3" - Caso 4
				level.add(1, "n_exact_3");
				return level;
			}
			if (returnCase == 2 && nameCase == 1 && paramCase == 1 && excepCase == 1) {
				level.add(0, 5); // "n_exact_4" - Caso 5
				level.add(1, "n_exact_4");
				return level;
			}
			if (returnCase == 1 && nameCase == 1 && paramCase == 1 && excepCase == 3) {
				level.add(0, 6); // "n_exact_5" - Caso 6
				level.add(1, "n_exact_5");
				return level;
			}
			if (returnCase == 1 && nameCase == 2 && paramCase == 1 && excepCase == 2) {
				level.add(0, 7); // "n_exact_6" - Caso 7
				level.add(1, "n_exact_6");
				return level;
			}
			if (returnCase == 1 && nameCase == 3 && paramCase == 1 && excepCase == 1) {
				level.add(0, 8); // "n_exact_7" - Caso 8
				level.add(1, "n_exact_7");
				return level;
			}
			if (returnCase == 1 && nameCase == 1 && paramCase == 2 && excepCase == 2) {
				level.add(0, 9); // "n_exact_8" - Caso 9
				level.add(1, "n_exact_8");
				return level;
			}
			if (returnCase == 1 && nameCase == 2 && paramCase == 2 && excepCase == 1) {
				level.add(0, 10); // "n_exact_9" - Caso 10
				level.add(1, "n_exact_9");
				return level;
			}
			if (returnCase == 1 && nameCase == 1 && paramCase == 3 && excepCase == 1) {
				level.add(0, 11); // "n_exact_10" - Caso 11
				level.add(1, "n_exact_10");
				return level;
			}
			if (returnCase == 2 && nameCase == 1 && paramCase == 1 && excepCase == 2) {
				level.add(0, 12); // "n_exact_11" - Caso 12
				level.add(1, "n_exact_11");
				return level;
			}
			if (returnCase == 2 && nameCase == 2 && paramCase == 1 && excepCase == 1) {
				level.add(0, 13); // "n_exact_12" - Caso 13
				level.add(1, "n_exact_12");
				return level;
			}
			if (returnCase == 2 && nameCase == 1 && paramCase == 2 && excepCase == 1) {
				level.add(0, 14); // "n_exact_13" - Caso 14
				level.add(1, "n_exact_13");
				return level;
			}

			// --[ Soft
			// ]--------------------------------------------------------------------------

			if (returnCase == 1 && nameCase == 2 && paramCase == 1 && excepCase == 3) {
				level.add(0, 16); // "soft_1" - Caso 16
				level.add(1, "soft_1");
				return level;
			}
			if (returnCase == 1 && nameCase == 3 && paramCase == 1 && excepCase == 2) {
				level.add(0, 17); // "soft_2" - Caso 17
				level.add(1, "soft_2");
				return level;
			}
			if (returnCase == 1 && nameCase == 1 && paramCase == 2 && excepCase == 3) {
				level.add(0, 18); // "soft_3" - Caso 18
				level.add(1, "soft_3");
				return level;
			}
			if (returnCase == 1 && nameCase == 2 && paramCase == 2 && excepCase == 2) {
				level.add(0, 19); // "soft_4" - Caso 19
				level.add(1, "soft_4");
				return level;
			}
			if (returnCase == 1 && nameCase == 3 && paramCase == 2 && excepCase == 1) {
				level.add(0, 20); // "soft_5" - Caso 20
				level.add(1, "soft_5");
				return level;
			}
			if (returnCase == 1 && nameCase == 1 && paramCase == 3 && excepCase == 2) {
				level.add(0, 21); // "soft_6" - Caso 21
				level.add(1, "soft_6");
				return level;
			}
			if (returnCase == 1 && nameCase == 2 && paramCase == 3 && excepCase == 1) {
				level.add(0, 22); // "soft_7" - Caso 22
				level.add(1, "soft_7");
				return level;
			}
			if (returnCase == 2 && nameCase == 1 && paramCase == 1 && excepCase == 3) {
				level.add(0, 24); // "soft_9" - Caso 24
				level.add(1, "soft_9");
				return level;
			}
			if (returnCase == 2 && nameCase == 2 && paramCase == 1 && excepCase == 2) {
				level.add(0, 25); // "soft_10" - Caso 25
				level.add(1, "soft_10");
				return level;
			}
			if (returnCase == 2 && nameCase == 3 && paramCase == 1 && excepCase == 1) {
				level.add(0, 26); // "soft_11" - Caso 26
				level.add(1, "soft_11");
				return level;
			}
			if (returnCase == 2 && nameCase == 1 && paramCase == 2 && excepCase == 2) {
				level.add(0, 27); // "soft_12" - Caso 27
				level.add(1, "soft_12");
				return level;
			}
			if (returnCase == 2 && nameCase == 2 && paramCase == 2 && excepCase == 1) {
				level.add(0, 28); // "soft_13" - Caso 28
				level.add(1, "soft_13");
				return level;
			}
			if (returnCase == 2 && nameCase == 1 && paramCase == 3 && excepCase == 1) {
				level.add(0, 29); // "soft_14" - Caso 29
				level.add(1, "soft_14");
				return level;
			}
			if (returnCase == 1 && nameCase == 3 && paramCase == 1 && excepCase == 3) {
				level.add(0, 33); // "soft_18" - Caso 33
				level.add(1, "soft_18");
				return level;
			}
			if (returnCase == 1 && nameCase == 2 && paramCase == 2 && excepCase == 3) {
				level.add(0, 34); // "soft_19" - Caso 34
				level.add(1, "soft_19");
				return level;
			}
			if (returnCase == 1 && nameCase == 3 && paramCase == 2 && excepCase == 2) {
				level.add(0, 35); // "soft_20" - Caso 35
				level.add(1, "soft_20");
				return level;
			}
			if (returnCase == 1 && nameCase == 1 && paramCase == 3 && excepCase == 3) {
				level.add(0, 36); // "soft_21" - Caso 36
				level.add(1, "soft_21");
				return level;
			}
			if (returnCase == 1 && nameCase == 2 && paramCase == 3 && excepCase == 2) {
				level.add(0, 37); // "soft_22" - Caso 37
				level.add(1, "soft_22");
				return level;
			}
			if (returnCase == 1 && nameCase == 3 && paramCase == 3 && excepCase == 1) {
				level.add(0, 38); // "soft_23" - Caso 38
				level.add(1, "soft_23");
				return level;
			}
			if (returnCase == 2 && nameCase == 2 && paramCase == 1 && excepCase == 3) {
				level.add(0, 41); // "soft_26" - Caso 41
				level.add(1, "soft_26");
				return level;
			}
			if (returnCase == 2 && nameCase == 3 && paramCase == 1 && excepCase == 2) {
				level.add(0, 42); // "soft_27" - Caso 42
				level.add(1, "soft_27");
				return level;
			}
			if (returnCase == 2 && nameCase == 1 && paramCase == 2 && excepCase == 3) {
				level.add(0, 43); // "soft_28" - Caso 43
				level.add(1, "soft_28");
				return level;
			}
			if (returnCase == 2 && nameCase == 2 && paramCase == 2 && excepCase == 2) {
				level.add(0, 44); // "soft_29" - Caso 44
				level.add(1, "soft_29");
				return level;
			}
			if (returnCase == 2 && nameCase == 3 && paramCase == 2 && excepCase == 1) {
				level.add(0, 45); // "soft_30" - Caso 45
				level.add(1, "soft_30");
				return level;
			}
			if (returnCase == 2 && nameCase == 1 && paramCase == 3 && excepCase == 2) {
				level.add(0, 46); // "soft_31" - Caso 46
				level.add(1, "soft_31");
				return level;
			}
			if (returnCase == 2 && nameCase == 2 && paramCase == 3 && excepCase == 1) {
				level.add(0, 47); // "soft_32" - Caso 47
				level.add(1, "soft_32");
				return level;
			}

			// --[ N_Soft
			// ]------------------------------------------------------------------------

			if (returnCase == 1 && nameCase == 3 && paramCase == 2 && excepCase == 3) {
				level.add(0, 55); // "n_soft_1" - Caso 55
				level.add(1, "n_soft_1");
				return level;
			}
			if (returnCase == 1 && nameCase == 2 && paramCase == 3 && excepCase == 3) {
				level.add(0, 56); // "n_soft_2" - Caso 56
				level.add(1, "n_soft_2");
				return level;
			}
			if (returnCase == 1 && nameCase == 3 && paramCase == 3 && excepCase == 2) {
				level.add(0, 57); // "n_soft_3" - Caso 57
				level.add(1, "n_soft_3");
				return level;
			}
			if (returnCase == 2 && nameCase == 3 && paramCase == 1 && excepCase == 2) {
				level.add(0, 61); // "n_soft_7" - Caso 61
				level.add(1, "n_soft_7");
				return level;
			}
			if (returnCase == 2 && nameCase == 2 && paramCase == 2 && excepCase == 3) {
				level.add(0, 62); // "n_soft_8" - Caso 62
				level.add(1, "n_soft_8");
				return level;
			}
			if (returnCase == 2 && nameCase == 3 && paramCase == 2 && excepCase == 2) {
				level.add(0, 63); // "n_soft_9" - Caso 63
				level.add(1, "n_soft_9");
				return level;
			}
			if (returnCase == 2 && nameCase == 1 && paramCase == 3 && excepCase == 3) {
				level.add(0, 64); // "n_soft_10" - Caso 64
				level.add(1, "n_soft_10");
				return level;
			}
			if (returnCase == 2 && nameCase == 2 && paramCase == 3 && excepCase == 2) {
				level.add(0, 65); // "n_soft_11" - Caso 65
				level.add(1, "n_soft_11");
				return level;
			}
			if (returnCase == 2 && nameCase == 3 && paramCase == 3 && excepCase == 1) {
				level.add(0, 66); // "n_soft_12" - Caso 66
				level.add(1, "n_soft_12");
				return level;
			}
			if (returnCase == 1 && nameCase == 3 && paramCase == 3 && excepCase == 3) {
				level.add(0, 77); // "n_soft_23" - Caso 77
				level.add(1, "n_soft_23");
				return level;
			}
			if (returnCase == 2 && nameCase == 3 && paramCase == 2 && excepCase == 3) {
				level.add(0, 80); // "n_soft_26" - Caso 80
				level.add(1, "n_soft_26");
				return level;
			}
			if (returnCase == 2 && nameCase == 2 && paramCase == 3 && excepCase == 3) {
				level.add(0, 81); // "n_soft_27" - Caso 81
				level.add(1, "n_soft_27");
				return level;
			}
			if (returnCase == 2 && nameCase == 3 && paramCase == 3 && excepCase == 2) {
				level.add(0, 82); // "n_soft_28" - Caso 82
				level.add(1, "n_soft_28");
				return level;
			}
			if (returnCase == 2 && nameCase == 3 && paramCase == 3 && excepCase == 3) {
				level.add(0, 95); // "n_soft_41" - Caso 95
				level.add(1, "n_soft_41");
				return level;
			}
			if (returnCase == 1 && nameCase == 1 && paramCase == 4 && excepCase == 1) {
				level.add(0, 23); // "soft_8" - Caso 23
				level.add(1, "soft_8");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 1 && excepCase == 2) {
				level.add(0, 30); // "soft_15" - Caso 30
				level.add(1, "soft_15");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 1 && excepCase == 1) {
				level.add(0, 31); // "soft_16" - Caso 31
				level.add(1, "soft_16");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 2 && excepCase == 1) {
				level.add(0, 32); // "soft_17" - Caso 32
				level.add(1, "soft_17");
				return level;
			}
			if (returnCase == 1 && nameCase == 1 && paramCase == 4 && excepCase == 2) {
				level.add(0, 39); // "soft_24" - Caso 39
				level.add(1, "soft_24");
				return level;
			}
			if (returnCase == 1 && nameCase == 2 && paramCase == 4 && excepCase == 1) {
				level.add(0, 40); // "Soft_25" - Caso 40
				level.add(1, "soft_25");
				return level;
			}
			if (returnCase == 2 && nameCase == 1 && paramCase == 4 && excepCase == 1) {
				level.add(0, 48); // "Soft_33" - Caso 48
				level.add(1, "soft_33");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 1 && excepCase == 3) {
				level.add(0, 49); // "soft_34" - Caso 49
				level.add(1, "soft_34");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 1 && excepCase == 2) {
				level.add(0, 50); // "soft_35" - Caso 50
				level.add(1, "soft_35");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 1 && excepCase == 1) {
				level.add(0, 51); // "soft_36" - Caso 51
				level.add(1, "soft_36");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 2 && excepCase == 2) {
				level.add(0, 52); // "soft_37" - Caso 52
				level.add(1, "soft_37");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 2 && excepCase == 1) {
				level.add(0, 53); // "soft_38" - Caso 53
				level.add(1, "soft_38");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 3 && excepCase == 1) {
				level.add(0, 54); // "soft_39" - Caso 54
				level.add(1, "soft_39");
				return level;
			}

			// --[ N_Soft
			// ]------------------------------------------------------------------------

			if (returnCase == 1 && nameCase == 1 && paramCase == 4 && excepCase == 3) {
				level.add(0, 58); // "n_soft_4" - Caso 58
				level.add(1, "n_soft_4");
				return level;
			}
			if (returnCase == 1 && nameCase == 2 && paramCase == 4 && excepCase == 2) {
				level.add(0, 59); // "n_soft_5" - Caso 59
				level.add(1, "n_soft_5");
				return level;
			}
			if (returnCase == 1 && nameCase == 3 && paramCase == 4 && excepCase == 1) {
				level.add(0, 60); // "n_soft_6" - Caso 60
				level.add(1, "n_soft_6");
				return level;
			}
			if (returnCase == 2 && nameCase == 1 && paramCase == 4 && excepCase == 2) {
				level.add(0, 67); // "n_soft_13" - Caso 67
				level.add(1, "n_soft_13");
				return level;
			}
			if (returnCase == 2 && nameCase == 2 && paramCase == 4 && excepCase == 1) {
				level.add(0, 68); // "n_soft_14" - Caso 68
				level.add(1, "n_soft_14");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 1 && excepCase == 3) {
				level.add(0, 69); // "n_soft_15" - Caso 69
				level.add(1, "n_soft_15");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 1 && excepCase == 2) {
				level.add(0, 70); // "n_soft_16" - Caso 70
				level.add(1, "n_soft_16");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 2 && excepCase == 3) {
				level.add(0, 71); // "n_soft_17" - Caso 71
				level.add(1, "n_soft_17");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 2 && excepCase == 2) {
				level.add(0, 72); // "n_soft_18" - Caso 72
				level.add(1, "n_soft_18");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 2 && excepCase == 1) {
				level.add(0, 73); // "n_soft_19" - Caso 73
				level.add(1, "n_soft_19");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 3 && excepCase == 2) {
				level.add(0, 74); // "n_soft_20" - Caso 74
				level.add(1, "n_soft_20");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 3 && excepCase == 1) {
				level.add(0, 75); // "n_soft_21" - Caso 75
				level.add(1, "n_soft_21");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 4 && excepCase == 1) {
				level.add(0, 76); // "n_soft_22" - Caso 76
				level.add(1, "n_soft_22");
				return level;
			}
			if (returnCase == 1 && nameCase == 2 && paramCase == 4 && excepCase == 3) {
				level.add(0, 78); // "n_soft_24" - Caso 78
				level.add(1, "n_soft_24");
				return level;
			}

			if (returnCase == 1 && nameCase == 3 && paramCase == 4 && excepCase == 2) {
				level.add(0, 79); // "n_soft_25" - Caso 79
				level.add(1, "n_soft_25");
				return level;
			}
			if (returnCase == 2 && nameCase == 1 && paramCase == 4 && excepCase == 3) {
				level.add(0, 83); // "n_soft_29" - Caso 83
				level.add(1, "n_soft_29");
				return level;
			}
			if (returnCase == 2 && nameCase == 2 && paramCase == 4 && excepCase == 2) {
				level.add(0, 84); // "n_soft_30" - Caso 84
				level.add(1, "n_soft_30");
				return level;
			}
			if (returnCase == 2 && nameCase == 3 && paramCase == 4 && excepCase == 1) {
				level.add(0, 85); // "n_soft_31" - Caso 85
				level.add(1, "n_soft_31");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 1 && excepCase == 3) {
				level.add(0, 86); // "n_soft_32" - Caso 86
				level.add(1, "n_soft_32");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 2 && excepCase == 3) {
				level.add(0, 87); // "n_soft_33" - Caso 87
				level.add(1, "n_soft_33");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 3 && excepCase == 2) {
				level.add(0, 88); // "n_soft_34" - Caso 88
				level.add(1, "n_soft_34");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 3 && excepCase == 3) {
				level.add(0, 89); // "n_soft_35" - Caso 89
				level.add(1, "n_soft_35");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 3 && excepCase == 2) {
				level.add(0, 90); // "n_soft_36" - Caso 90
				level.add(1, "n_soft_36");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 3 && excepCase == 1) {
				level.add(0, 91); // "n_soft_37" - Caso 91
				level.add(1, "n_soft_37");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 4 && excepCase == 2) {
				level.add(0, 92); // "n_soft_38" - Caso 92
				level.add(1, "n_soft_38");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 4 && excepCase == 1) {
				level.add(0, 93); // "n_soft_39" - Caso 93
				level.add(1, "n_soft_39");
				return level;
			}
			if (returnCase == 1 && nameCase == 3 && paramCase == 4 && excepCase == 3) {
				level.add(0, 94); // "n_soft_40" - Caso 94
				level.add(1, "n_soft_40");
				return level;
			}
			if (returnCase == 2 && nameCase == 2 && paramCase == 4 && excepCase == 3) {
				level.add(0, 96); // "n_soft_3" - Caso 57
				level.add(1, "n_soft_42");
				return level;
			}
			if (returnCase == 2 && nameCase == 3 && paramCase == 4 && excepCase == 2) {
				level.add(0, 97); // "n_soft_43" - Caso 97
				level.add(1, "n_soft_43");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 2 && excepCase == 3) {
				level.add(0, 98); // "n_soft_44" - Caso 98
				level.add(1, "n_soft_44");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 3 && excepCase == 3) {
				level.add(0, 99); // "n_soft_45" - Caso 99
				level.add(1, "n_soft_45");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 3 && excepCase == 2) {
				level.add(0, 100); // "n_soft_46" - Caso 100
				level.add(1, "n_soft_46");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 4 && excepCase == 3) {
				level.add(0, 101); // "n_soft_47" - Caso 101
				level.add(1, "n_soft_47");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 4 && excepCase == 2) {
				level.add(0, 102); // "n_soft_48" - Caso 102
				level.add(1, "n_soft_48");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 4 && excepCase == 1) {
				level.add(0, 103); // "n_soft_49" - Caso 103
				level.add(1, "n_soft_49");
				return level;
			}
			if (returnCase == 2 && nameCase == 3 && paramCase == 4 && excepCase == 3) {
				level.add(0, 104); // "n_soft_50" - Caso 104
				level.add(1, "n_soft_50");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 3 && excepCase == 3) {
				level.add(0, 105); // "n_soft_51" - Caso 105
				level.add(1, "n_soft_51");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 4 && excepCase == 3) {
				level.add(0, 106); // "n_soft_52" - Caso 106
				level.add(1, "n_soft_52");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 4 && excepCase == 2) {
				level.add(0, 107); // "n_soft_53" - Caso 107
				level.add(1, "n_soft_53");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 4 && excepCase == 3) {
				level.add(0, 108); // "n_soft_54" - Caso 108
				level.add(1, "n_soft_54");
				return level;
			}
			if (returnCase == 1 && nameCase == 4 && paramCase == 1 && excepCase == 1) {
				level.add(0, 109); // "n_soft_55" - Caso 109
				level.add(1, "n_soft_55");
				return level;
			}

			if (returnCase == 2 && nameCase == 4 && paramCase == 1 && excepCase == 1) {
				level.add(0, 110); // "n_soft_56" - Caso 110
				level.add(1, "n_soft_56");
				return level;
			}

			if (returnCase == 3 && nameCase == 4 && paramCase == 1 && excepCase == 1) {
				level.add(0, 111); // "n_soft_57" - Caso 111
				level.add(1, "n_soft_57");
				return level;
			}

			if (returnCase == 4 && nameCase == 1 && paramCase == 1 && excepCase == 1) {
				level.add(0, 112); // "n_soft_58" - Caso 112
				level.add(1, "n_soft_58");
				return level;
			}

			if (returnCase == 4 && nameCase == 2 && paramCase == 1 && excepCase == 1) {
				level.add(0, 113); // "n_soft_59" - Caso 113
				level.add(1, "n_soft_59");
				return level;
			}

			if (returnCase == 4 && nameCase == 3 && paramCase == 1 && excepCase == 1) {
				level.add(0, 114); // "n_soft_60" - Caso 114
				level.add(1, "n_soft_60");
				return level;
			}

			if (returnCase == 4 && nameCase == 4 && paramCase == 1 && excepCase == 1) {
				level.add(0, 115); // "n_soft_61" - Caso 115
				level.add(1, "n_soft_61");
				return level;
			}

			if (returnCase == 1 && nameCase == 4 && paramCase == 2 && excepCase == 1) {
				level.add(0, 116); // "n_soft_62" - Caso 116
				level.add(1, "n_soft_62");
				return level;
			}

			if (returnCase == 2 && nameCase == 4 && paramCase == 2 && excepCase == 1) {
				level.add(0, 117); // "n_soft_63" - Caso 117
				level.add(1, "n_soft_63");
				return level;
			}

			if (returnCase == 3 && nameCase == 4 && paramCase == 2 && excepCase == 1) {
				level.add(0, 118); // "n_soft_64" - Caso 118
				level.add(1, "n_soft_64");
				return level;
			}

			if (returnCase == 4 && nameCase == 1 && paramCase == 2 && excepCase == 1) {
				level.add(0, 119); // "n_soft_65" - Caso 119
				level.add(1, "n_soft_65");
				return level;
			}

			if (returnCase == 4 && nameCase == 2 && paramCase == 2 && excepCase == 1) {
				level.add(0, 120); // "n_soft_66" - Caso 120
				level.add(1, "n_soft_66");
				return level;
			}

			if (returnCase == 4 && nameCase == 3 && paramCase == 2 && excepCase == 1) {
				level.add(0, 121); // "n_soft_67" - Caso 121
				level.add(1, "n_soft_67");
				return level;
			}

			if (returnCase == 4 && nameCase == 4 && paramCase == 2 && excepCase == 1) {
				level.add(0, 122); // "n_soft_68" - Caso 122
				level.add(1, "n_soft_68");
				return level;
			}

			if (returnCase == 1 && nameCase == 4 && paramCase == 3 && excepCase == 1) {
				level.add(0, 123); // "n_soft_69" - Caso 123
				level.add(1, "n_soft_69");
				return level;
			}

			if (returnCase == 2 && nameCase == 4 && paramCase == 3 && excepCase == 1) {
				level.add(0, 124); // "n_soft_70" - Caso 124
				level.add(1, "n_soft_70");
				return level;
			}

			if (returnCase == 3 && nameCase == 4 && paramCase == 3 && excepCase == 1) {
				level.add(0, 125); // "n_soft_71" - Caso 125
				level.add(1, "n_soft_71");
				return level;
			}

			if (returnCase == 4 && nameCase == 1 && paramCase == 3 && excepCase == 1) {
				level.add(0, 126); // "n_soft_72" - Caso 126
				level.add(1, "n_soft_72");
				return level;
			}

			if (returnCase == 4 && nameCase == 2 && paramCase == 3 && excepCase == 1) {
				level.add(0, 127); // "n_soft_73" - Caso 127
				level.add(1, "n_soft_73");
				return level;
			}

			if (returnCase == 4 && nameCase == 3 && paramCase == 3 && excepCase == 1) {
				level.add(0, 128); // "n_soft_74" - Caso 128
				level.add(1, "n_soft_74");
				return level;
			}

			if (returnCase == 4 && nameCase == 4 && paramCase == 3 && excepCase == 1) {
				level.add(0, 129); // "n_soft_75" - Caso 129
				level.add(1, "n_soft_75");
				return level;
			}

			if (returnCase == 1 && nameCase == 4 && paramCase == 4 && excepCase == 1) {
				level.add(0, 130); // "n_soft_76" - Caso 130
				level.add(1, "n_soft_76");
				return level;
			}

			if (returnCase == 2 && nameCase == 4 && paramCase == 4 && excepCase == 1) {
				level.add(0, 131); // "n_soft_77" - Caso 131
				level.add(1, "n_soft_77");
				return level;
			}

			if (returnCase == 3 && nameCase == 4 && paramCase == 4 && excepCase == 1) {
				level.add(0, 132); // "n_soft_78" - Caso 132
				level.add(1, "n_soft_78");
				return level;
			}

			if (returnCase == 3 && nameCase == 4 && paramCase == 4 && excepCase == 1) {
				level.add(0, 132); // "n_soft_78" - Caso 132
				level.add(1, "n_soft_78");
				return level;
			}

			if (returnCase == 4 && nameCase == 1 && paramCase == 4 && excepCase == 1) {
				level.add(0, 133); // "n_soft_79" - Caso 133
				level.add(1, "n_soft_79");
				return level;
			}

			if (returnCase == 4 && nameCase == 2 && paramCase == 4 && excepCase == 1) {
				level.add(0, 134); // "n_soft_80" - Caso 134
				level.add(1, "n_soft_80");
				return level;
			}

			if (returnCase == 4 && nameCase == 3 && paramCase == 4 && excepCase == 1) {
				level.add(0, 135); // "n_soft_81" - Caso 135
				level.add(1, "n_soft_81");
				return level;
			}

			if (returnCase == 4 && nameCase == 4 && paramCase == 4 && excepCase == 1) {
				level.add(0, 136); // "n_soft_82" - Caso 136
				level.add(1, "n_soft_82");
				return level;
			}

			level.add(0, 137); // Tener en cuenta el Sort para poner mas
								// numeros
			level.add(1, "misterioso");
		} else {
			level.add(new Integer(NO_COMPATIBLE));
		}
		return level;
	}

	// Falta arreglar los niveles en los que se agregan las compatibilidades
	// nuevas,
	// para que queden primeras en las listas respectivas. Deben ser nros
	// enteros!!
	private Vector getLevelCompatibilidadManual(int returnCase, int nameCase, int paramCase, int excepCase) {
		// Idenfica el caso de compatibilidad
		Vector level = new Vector();
		if (paramCase != 0 && returnCase != 0 && excepCase != 0) {
			String rCase = "R" + returnCase;
			String nCase = "N" + nameCase;
			String pCase = "P" + paramCase;
			String eCase = "E" + excepCase;
			level.add(rCase);
			level.add(nCase);
			level.add(pCase);
			level.add(eCase);

			// 54 Casos manuales, chequeados. R puede ser 1, 2 � 3.
			// P puede ser 1,2,3 � 4.
			// N, E pueden ser 1,2 � 3.

			// --[ N_Exact
			// ]-----------------------------------------------------------------------

			if (returnCase == 3 && nameCase == 1 && paramCase == 1 && excepCase == 1) {
				level.add(0, 15); // "n_exact_14" - Caso 15
				level.add(1, "n_exact_14");
				return level;
			}

			// --[ Soft
			// ]--------------------------------------------------------------------------

			if (returnCase == 1 && nameCase == 1 && paramCase == 4 && excepCase == 1) {
				level.add(0, 23); // "soft_8" - Caso 23
				level.add(1, "soft_8");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 1 && excepCase == 2) {
				level.add(0, 30); // "soft_15" - Caso 30
				level.add(1, "soft_15");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 1 && excepCase == 1) {
				level.add(0, 31); // "soft_16" - Caso 31
				level.add(1, "soft_16");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 2 && excepCase == 1) {
				level.add(0, 32); // "soft_17" - Caso 32
				level.add(1, "soft_17");
				return level;
			}
			if (returnCase == 1 && nameCase == 1 && paramCase == 4 && excepCase == 2) {
				level.add(0, 39); // "soft_24" - Caso 39
				level.add(1, "soft_24");
				return level;
			}
			if (returnCase == 1 && nameCase == 2 && paramCase == 4 && excepCase == 1) {
				level.add(0, 40); // "Soft_25" - Caso 40
				level.add(1, "soft_25");
				return level;
			}
			if (returnCase == 2 && nameCase == 1 && paramCase == 4 && excepCase == 1) {
				level.add(0, 48); // "Soft_33" - Caso 48
				level.add(1, "soft_33");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 1 && excepCase == 3) {
				level.add(0, 49); // "soft_34" - Caso 49
				level.add(1, "soft_34");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 1 && excepCase == 2) {
				level.add(0, 50); // "soft_35" - Caso 50
				level.add(1, "soft_35");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 1 && excepCase == 1) {
				level.add(0, 51); // "soft_36" - Caso 51
				level.add(1, "soft_36");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 2 && excepCase == 2) {
				level.add(0, 52); // "soft_37" - Caso 52
				level.add(1, "soft_37");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 2 && excepCase == 1) {
				level.add(0, 53); // "soft_38" - Caso 53
				level.add(1, "soft_38");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 3 && excepCase == 1) {
				level.add(0, 54); // "soft_39" - Caso 54
				level.add(1, "soft_39");
				return level;
			}

			// --[ N_Soft
			// ]------------------------------------------------------------------------

			if (returnCase == 1 && nameCase == 1 && paramCase == 4 && excepCase == 3) {
				level.add(0, 58); // "n_soft_4" - Caso 58
				level.add(1, "n_soft_4");
				return level;
			}
			if (returnCase == 1 && nameCase == 2 && paramCase == 4 && excepCase == 2) {
				level.add(0, 59); // "n_soft_5" - Caso 59
				level.add(1, "n_soft_5");
				return level;
			}
			if (returnCase == 1 && nameCase == 3 && paramCase == 4 && excepCase == 1) {
				level.add(0, 60); // "n_soft_6" - Caso 60
				level.add(1, "n_soft_6");
				return level;
			}
			if (returnCase == 2 && nameCase == 1 && paramCase == 4 && excepCase == 2) {
				level.add(0, 67); // "n_soft_13" - Caso 67
				level.add(1, "n_soft_13");
				return level;
			}
			if (returnCase == 2 && nameCase == 2 && paramCase == 4 && excepCase == 1) {
				level.add(0, 68); // "n_soft_14" - Caso 68
				level.add(1, "n_soft_14");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 1 && excepCase == 3) {
				level.add(0, 69); // "n_soft_15" - Caso 69
				level.add(1, "n_soft_15");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 1 && excepCase == 2) {
				level.add(0, 70); // "n_soft_16" - Caso 70
				level.add(1, "n_soft_16");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 2 && excepCase == 3) {
				level.add(0, 71); // "n_soft_17" - Caso 71
				level.add(1, "n_soft_17");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 2 && excepCase == 2) {
				level.add(0, 72); // "n_soft_18" - Caso 72
				level.add(1, "n_soft_18");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 2 && excepCase == 1) {
				level.add(0, 73); // "n_soft_19" - Caso 73
				level.add(1, "n_soft_19");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 3 && excepCase == 2) {
				level.add(0, 74); // "n_soft_20" - Caso 74
				level.add(1, "n_soft_20");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 3 && excepCase == 1) {
				level.add(0, 75); // "n_soft_21" - Caso 75
				level.add(1, "n_soft_21");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 4 && excepCase == 1) {
				level.add(0, 76); // "n_soft_22" - Caso 76
				level.add(1, "n_soft_22");
				return level;
			}
			if (returnCase == 1 && nameCase == 2 && paramCase == 4 && excepCase == 3) {
				level.add(0, 78); // "n_soft_24" - Caso 78
				level.add(1, "n_soft_24");
				return level;
			}

			if (returnCase == 1 && nameCase == 3 && paramCase == 4 && excepCase == 2) {
				level.add(0, 79); // "n_soft_25" - Caso 79
				level.add(1, "n_soft_25");
				return level;
			}
			if (returnCase == 2 && nameCase == 1 && paramCase == 4 && excepCase == 3) {
				level.add(0, 83); // "n_soft_29" - Caso 83
				level.add(1, "n_soft_29");
				return level;
			}
			if (returnCase == 2 && nameCase == 2 && paramCase == 4 && excepCase == 2) {
				level.add(0, 84); // "n_soft_30" - Caso 84
				level.add(1, "n_soft_30");
				return level;
			}
			if (returnCase == 2 && nameCase == 3 && paramCase == 4 && excepCase == 1) {
				level.add(0, 85); // "n_soft_31" - Caso 85
				level.add(1, "n_soft_31");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 1 && excepCase == 3) {
				level.add(0, 86); // "n_soft_32" - Caso 86
				level.add(1, "n_soft_32");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 2 && excepCase == 3) {
				level.add(0, 87); // "n_soft_33" - Caso 87
				level.add(1, "n_soft_33");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 3 && excepCase == 2) {
				level.add(0, 88); // "n_soft_34" - Caso 88
				level.add(1, "n_soft_34");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 3 && excepCase == 3) {
				level.add(0, 89); // "n_soft_35" - Caso 89
				level.add(1, "n_soft_35");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 3 && excepCase == 2) {
				level.add(0, 90); // "n_soft_36" - Caso 90
				level.add(1, "n_soft_36");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 3 && excepCase == 1) {
				level.add(0, 91); // "n_soft_37" - Caso 91
				level.add(1, "n_soft_37");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 4 && excepCase == 2) {
				level.add(0, 92); // "n_soft_38" - Caso 92
				level.add(1, "n_soft_38");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 4 && excepCase == 1) {
				level.add(0, 93); // "n_soft_39" - Caso 93
				level.add(1, "n_soft_39");
				return level;
			}
			if (returnCase == 1 && nameCase == 3 && paramCase == 4 && excepCase == 3) {
				level.add(0, 94); // "n_soft_40" - Caso 94
				level.add(1, "n_soft_40");
				return level;
			}
			if (returnCase == 2 && nameCase == 2 && paramCase == 4 && excepCase == 3) {
				level.add(0, 96); // "n_soft_3" - Caso 57
				level.add(1, "n_soft_42");
				return level;
			}
			if (returnCase == 2 && nameCase == 3 && paramCase == 4 && excepCase == 2) {
				level.add(0, 97); // "n_soft_43" - Caso 97
				level.add(1, "n_soft_43");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 2 && excepCase == 3) {
				level.add(0, 98); // "n_soft_44" - Caso 98
				level.add(1, "n_soft_44");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 3 && excepCase == 3) {
				level.add(0, 99); // "n_soft_45" - Caso 99
				level.add(1, "n_soft_45");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 3 && excepCase == 2) {
				level.add(0, 100); // "n_soft_46" - Caso 100
				level.add(1, "n_soft_46");
				return level;
			}
			if (returnCase == 3 && nameCase == 1 && paramCase == 4 && excepCase == 3) {
				level.add(0, 101); // "n_soft_47" - Caso 101
				level.add(1, "n_soft_47");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 4 && excepCase == 2) {
				level.add(0, 102); // "n_soft_48" - Caso 102
				level.add(1, "n_soft_48");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 4 && excepCase == 1) {
				level.add(0, 103); // "n_soft_49" - Caso 103
				level.add(1, "n_soft_49");
				return level;
			}
			if (returnCase == 2 && nameCase == 3 && paramCase == 4 && excepCase == 3) {
				level.add(0, 104); // "n_soft_50" - Caso 104
				level.add(1, "n_soft_50");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 3 && excepCase == 3) {
				level.add(0, 105); // "n_soft_51" - Caso 105
				level.add(1, "n_soft_51");
				return level;
			}
			if (returnCase == 3 && nameCase == 2 && paramCase == 4 && excepCase == 3) {
				level.add(0, 106); // "n_soft_52" - Caso 106
				level.add(1, "n_soft_52");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 4 && excepCase == 2) {
				level.add(0, 107); // "n_soft_53" - Caso 107
				level.add(1, "n_soft_53");
				return level;
			}
			if (returnCase == 3 && nameCase == 3 && paramCase == 4 && excepCase == 3) {
				level.add(0, 108); // "n_soft_54" - Caso 108
				level.add(1, "n_soft_54");
				return level;
			}
			level.add(0, 109); // Tener en cuenta el Sort para poner mas
			// numeros
			level.add(1, "misterioso");
		} else {
			level.add(new Integer(NO_COMPATIBLE));
		}
		return level;
	}

	// private double isSubTypeReturn(Operation requiredOperation, Operation
	// candidateOperation)
	// { double complexReturnTypeResult=0.0, returnTypeResult, nameResult;
	// int ret;
	// Vector vecStems = new Vector();
	// int[] nameResultArray;
	// //si no son primitivos los tipos de datos
	// if
	// (!(Agente.getAgente().esPrimitivo(requiredOperation.getResponse().getType().getName()))
	// &&
	// !(Agente.getAgente().esPrimitivo(candidateOperation.getResponse().getType().getName())))
	// {
	// //calcula compatibilidad de los atributos de los tipos complejos
	// returnTypeResult =
	// SemanticInterfaceCompatibility.fieldCaseValue(requiredOperation,candidateOperation);
	// //calcula la compatibilidad d elos nombres de los tipos compljos :
	// cliente con customer por ejemplo
	// nameResultArray =
	// SemanticInterfaceCompatibility.assessNameCompatibility(requiredOperation.getResponse().getType().getName(),
	// candidateOperation.getResponse().getType().getName(), vecStems);
	// //obtienen un solo valor de compatibilidad para los nombres
	// nameResult =
	// SemanticInterfaceCompatibility.compatibleValue(nameResultArray,(Vector)
	// vecStems.get(0), (Vector)vecStems.get(1));
	// //valor de compatibilidad final que engloba lo calculado anteriormente
	// complexReturnTypeResult = (returnTypeResult * (1 + nameResult))/2;
	// //Agregu� la divisi�n por 2 en �ste punto y no en la
	// discretizaci�n.
	//
	// }
	//
	// // PRIMITIVO VS.COMPLEJO
	// else
	// {
	// //Preguntar si candidateOperation es complejo, y si hay algun campo que
	// coincida con el tipo de retorno esperado por requiredOperation.
	// //Recorrer los fields del tipo de candidateOperation y ver si alguno es
	// del mismo tipo (equals) o subtipo (con isSubTyping) que
	// requiredOperation.
	// //En ese caso devolver un valor double que segun la discretizacion sea R3
	//
	// //if (Agente.getAgente().esPrimitivo(requiredOperation.getTipo()) &&
	// !(Agente.getAgente().esPrimitivo(candidateOperation.getTipo())))
	// if
	// (Agente.getAgente().esPrimitivo(requiredOperation.getResponse().getType().getName())
	// &&
	// !(Agente.getAgente().esPrimitivo(candidateOperation.getResponse().getType().getName())))
	// complexReturnTypeResult =
	// SemanticInterfaceCompatibility.simpleFieldCaseValue(requiredOperation,candidateOperation);
	// }
	// return complexReturnTypeResult;
	// }

	public static double calculateTypeCompatibility(Type requiredType, Type candidateType) throws IOException {
		if (isSubTyping(requiredType.getName(), candidateType.getName()))
			return 1.5;
		// Comparacion tipos complejos
		else {
			if (requiredType.isArray()&&candidateType.isArray())
			{
				ArrayType arrayRequiredType= (ArrayType)requiredType;
				ArrayType arrayCandidateType=(ArrayType)candidateType;
				return calculateTypeCompatibility(arrayRequiredType.getContentType(), arrayCandidateType.getContentType());
			}
			else
			{
				if(requiredType.isComplexType()&&candidateType.isComplexType())
				{
					//si los dos son array comparar los content de los array. Si uno es y el otro no retornar 1.
					double cplxParamTypeResult, cplxNameResult, complexReturnTypeResult;
					cplxParamTypeResult = 0;
					Vector vecStems = new Vector();
					int[] cplxNameResultArray;
	
					//System.out.println("Nombre del Tipo Complejo del Param1: " + c1Param.getType().getName());
					//System.out.println("Nombre del Tipo Complejo del Param2: " + c2Param.getType().getName());
	
					//que tan compatible es estructuralmente el tipo NO SIMPLE
					cplxParamTypeResult = SemanticInterfaceCompatibility.fieldCaseValueParam(requiredType, candidateType);
	
					cplxNameResultArray = SemanticInterfaceCompatibility
							.assessNameCompatibility(requiredType.getName(), candidateType.getName(), vecStems);
					//que tan compatible son los nombres de los tipos NO SIMPLES
					cplxNameResult = SemanticInterfaceCompatibility.compatibleValue(cplxNameResultArray,
							(Vector) vecStems.get(0), (Vector) vecStems.get(1));
	
					complexReturnTypeResult = (cplxParamTypeResult * (1 + cplxNameResult));
	
					//System.out.println("ret isSubTypeParameters " + complexReturnTypeResult);
					return complexReturnTypeResult;
				}
				else return 1;
			}
		}
	}

	// CLASES ENVOLVENTES - en orden de menor a mayor capacidad.

	// byte --> Byte
	// short --> Short
	// char --> Character
	// int --> Integer
	// long --> Long
	// float --> Float
	// double --> Double
	// �BOOLEAN?

	// Se incluyeron todas las conversiones sin p�rdida de precision, seg�n
	// nueva def. R2
	// y la compatibilidad de String con cualquier tipo.
	// M.G. 14/09/2010 Agregados arrays de tipos simples EN PARAMETROS.
	// Significado:
	// [B --> array de byte
	// [S --> array de short
	// [I --> array de int
	// [J --> array de long, prestar atencion! es J y no L como uno supondr�a!
	// [F --> array de float
	// [D --> array de double

	public static boolean isSubTyping(String type1, String type2) {
		boolean res = false;
		String lstByte = "byte,Byte,short,Short,int,Integer,long,Long,float,Float,double,Double,String";
		String lstShort = "short,Short,int,Integer,long,Long,float,Float,double,Double,String";
		String lstInt = "int,Integer,long,Long,float,Float,java.lang.double,java.lang.Double,double,Double,String";
		String lstLong = "long,Long,float,Float,double,Double,String";
		String lstFloat = "float,Float,double,Double,java.lang.String";
		String lstChar = "char,Character,String";
		// String lstDouble = "double,Double,String";
		// String lstDouble =
		// "java.lang.double,java.lang.Double,double,Double,long,Long,String,int,Integer";
		String lstDouble = "java.lang.double,java.lang.Double,double,Double,long,Long,String";
		String lstarrayByte = "[B,[S,[I,[J,[F,[D";
		String lstarrayShort = "[S,[I,[J,[F,[D";
		String lstarrayInt = "[I,[J,[F,[D";
		String lstarrayLong = "[J,[F,[D";
		String lstarrayFloat = "[F,[D";
		String lstarrayDouble = "[D";
		String lstarrayChar = "[C";

		// Si el tipo esperado es String, aceptamos cualquier cosa
		if (type1 == "java.lang.String") {
			res = true;
			return res;
		}
		// An�lisis de tipos primitivos
		if ((type1 == "char") || (type1 == "Character"))
			if (lstChar.indexOf(type2) != -1) {
				res = true;
				return res;
			}
		if ((type1 == "byte") || (type1 == "Byte"))
			if (lstByte.indexOf(type2) != -1) {
				res = true;
				return res;
			}
		if ((type1 == "short") || (type1 == "Short"))
			if (lstShort.indexOf(type2) != -1) {
				res = true;
				return res;
			}
		if ((type1 == "int") || (type1 == "Integer"))
			if (lstInt.indexOf(type2) != -1) {
				res = true;
				return res;
			}
		if ((type1 == "long") || (type1 == "Long"))
			if (lstLong.indexOf(type2) != -1) {
				res = true;
				return res;
			}
		if ((type1 == "float") || (type1 == "Float"))
			if (lstFloat.indexOf(type2) != -1) {
				res = true;
				return res;
			}
		// if ((type1 == "double") || (type1 == "Double"))
		if (type1.toLowerCase().equals("java.lang.double") || (type1.toLowerCase().equals("java.lang.Double"))
				|| (type1 == "double") || (type1 == "Double"))
			if (lstDouble.indexOf(type2) != -1) {
				res = true;
				return res;
			}

		// // Analisis de ARRAYS de tipos primitivos!
		if (type1 == "[C")
			if (lstarrayChar.indexOf(type2) != -1) {
				res = true;
				return res;
			}
		if (type1 == "[B")
			if (lstarrayByte.indexOf(type2) != -1) {
				res = true;
				return res;
			}
		if (type1 == "[S")
			if (lstarrayShort.indexOf(type2) != -1) {
				res = true;
				return res;
			}
		if (type1 == "[I")
			if (lstarrayInt.indexOf(type2) != -1) {
				res = true;
				return res;
			}
		if (type1 == "[J")
			if (lstarrayLong.indexOf(type2) != -1) {
				res = true;
				return res;
			}
		if (type1 == "[F")
			if (lstarrayFloat.indexOf(type2) != -1) {
				res = true;
				return res;
			}
		if (type1 == "[D")
			if (lstarrayDouble.indexOf(type2) != -1) {
				res = true;
				return res;
			}
		return res;
	}

	private Vector addCompatibleMethod(Vector compatibles, Operation c2Operation, Vector level,
			double adaptabilityGap) {
		// no envio el HashTable, sino solo el vector..
		// level.add(2,c2Method.getWholeSignature()); // nivel de compatibility
		level.add(2, c2Operation);
		// level.add(3,adaptabilityGap);
		level.add(3, adaptabilityGap);

		if (compatibles == null) {
			compatibles = new Vector();
		}
		compatibles.add(level);
		return compatibles;
	}

	/**
	 * -----------------------------------------------------------------
	 * Ordenamiento en O(n). Se usa cuando las claves de b�squeda est�n
	 * dentro de un rango determinado (finito y conocido). Algoritmo:
	 * enumeraci�n por Distribuci�n
	 */

	public Vector getSortedVector(Vector a, int rango) {
		int max = a.size();
		Vector[] salida = new Vector[max];
		Integer clave = new Integer(0);
		int[] count = new int[rango];

		for (int i = 0; i < max; i++) {
			// cuenta las veces que se repite una clave en count.
			clave = (Integer) ((Vector) a.get(i)).get(0);
			count[clave.intValue()]++;
		}

		for (int i = 1; i < rango; i++) {
			// calcula la posicion m�s a la derecha de una clave.+1
			count[i] = count[i - 1] + count[i];
		}

		for (int i = max - 1; i >= 0; i--) {
			// ubica al elemento en la posicion que corresponde.
			clave = (Integer) ((Vector) a.get(i)).get(0);
			int j = count[clave.intValue()] - 1;
			salida[j] = (Vector) a.get(i);
			count[clave.intValue()]--;
		}

		Vector resultado = new Vector(max);
		for (int i = 0; i < max; i++) {
			resultado.add(salida[i]);
		}

		return resultado;
	}

	public String getSummary() {
		return summary;
	}

	public String getSummaryList() {
		return summaryList;
	}

	public Hashtable getCompatibilities() {
		return compatibilities;
	}

	public double getAdaptabilityGap() {
		double ret = 0;
		double minAdapMeth = 3;
		ArrayList<Operation> operations = requiredInterface.getOperations();
		for (Operation m : operations) {
			//System.out.println("************");
			//System.out.println(m.getName());
			Vector<Vector> compatibles = compatibilities.get(m.getName());
			// //System.out.println("Compatiblñes: "+compatibles.size());
			minAdapMeth = 3;
			for (Vector level : compatibles) {
				double adaptMethod = (Double) level.get(3);
				if (adaptMethod < minAdapMeth) {
					minAdapMeth = adaptMethod;
					Operation t = (Operation) level.get(2);
					//System.out.println(t.getName());
				}
			}
			ret = ret + minAdapMeth;
			//System.out.println("*--****---*****---**");
			//System.out.println();
		}
		if (operations.size() > 0)
			ret = ret / operations.size();
		return ret;
	}

} // Fin de Clase
