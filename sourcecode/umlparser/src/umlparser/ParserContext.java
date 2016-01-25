package umlparser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

public class ParserContext {
	public static class Association {
		String start;
		String end;
		String attribute;
		boolean isMul;
	}
	//public Map<String, Boolean> classMap;
	public List<CompilationUnit> cuList; 
	
	public StringBuilder sb;
	public List<String> result;
	public List<Association> aList;
	ClassOrInterface currentClass = null;
	
	public Map<String, ClassOrInterface> classes;
	
	public ParserContext() {
		cuList = new ArrayList<CompilationUnit>();
		//className = new HashSet<ClassOrInterface>();
		//sbList = new ArrayList<StringBuilder>();
		sb = new StringBuilder();
		aList = new ArrayList<Association>();
		result = new ArrayList<String>();
		classes = new HashMap<String, ClassOrInterface>();
		//classMap = new HashMap<String, Boolean>();
	}
	/*public boolean isCustomClass(String name) {
		return classMap.containsKey(name); 
	}*/
	public String print() {
		String res = "";
		for (String str : result) {
			res += str + ",";
		}
		return res;
	}
	
}
