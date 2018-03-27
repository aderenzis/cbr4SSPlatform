package compatibilityUtils;

import java.util.Vector;

public class Permutaciones {
	public static Vector<Vector<int[]>> permutaciones;
	
	
	
	private static void permutation(Vector<Integer> opRParams,Vector<Integer> opSParams, Vector<int[]>permutacion){
		if(opRParams.size()==1){
			for(Integer opSParam:opSParams){
				int[] corresp = {opRParams.get(0),opSParam};
				permutacion.add(corresp);
				permutaciones.add((Vector<int[]>)permutacion.clone());
				permutacion.remove(corresp);
			}
		}
		else if(opRParams.size()>0){
			for(int i=0;i< opSParams.size();i++){
				int[] corresp = {opRParams.get(0),opSParams.get(i)};
				Vector<int[]> permutacionAux = (Vector<int[]> )permutacion.clone();
				permutacionAux.add(corresp);
				//genero uno nuevo sin el agregado "corresp"
				Vector<Integer> opRParamsAux = (Vector<Integer>)opRParams.clone();
				opRParamsAux.removeElementAt(0);
				Vector<Integer> opSParamsAux = (Vector<Integer>)opSParams.clone();
				opSParamsAux.removeElementAt(i);
				permutation(opRParamsAux,opSParamsAux,permutacionAux);				
			}
		}
	}
	
	private static void permutationV2(Vector<Integer> opRParams,Vector<Integer> opSParams, Vector<int[]>permutacion){
		if(opSParams.size()==1){
			for(Integer opRParam:opRParams){
				int[] corresp = {opRParam,opSParams.get(0)};
				permutacion.add(corresp);
				permutaciones.add((Vector<int[]>)permutacion.clone());
				permutacion.remove(corresp);
			}
		}
		else{
			for(int i=0;i< opRParams.size();i++){
				int[] corresp = {opRParams.get(i),opSParams.get(0)};
				Vector<int[]> permutacionAux = (Vector<int[]> )permutacion.clone();
				permutacionAux.add(corresp);
				Vector<Integer> opRParamsAux = (Vector<Integer>)opRParams.clone();
				opRParamsAux.removeElementAt(i);
				Vector<Integer> opSParamsAux = (Vector<Integer>)opSParams.clone();
				opSParamsAux.removeElementAt(0);
				permutationV2(opRParamsAux,opSParamsAux,permutacionAux);				
			}
		}
	}
	
	
	public static Vector<Vector<int[]>> getPermutaciones (Vector<Integer> opRParams,Vector<Integer> opSParams){
		permutaciones=new Vector<Vector<int[]>>();
		permutation(opRParams,opSParams,new Vector<int[]>());
		return permutaciones;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Vector<Integer> v1= new Vector();
		v1.add(1);
		v1.add(2);
		Vector<Integer> v2= new Vector();
		v2.add(3);
		v2.add(4);
		v2.add(5);
		permutation(v1,v2,new Vector<int[]>());
		for (Vector<int[]> permutacion: permutaciones) {
			for (int[] corresp : permutacion){
				System.out.print(corresp[0]+ "-"+ corresp[1]+"   ");
			}
			System.out.println("");
		}
		
	}

}
