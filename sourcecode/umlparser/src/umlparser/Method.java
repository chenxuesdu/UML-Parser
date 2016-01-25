package umlparser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;

public class Method {
	public String name;
	public List<Parameter> paras;
	public Type returnType;
	public List<String> dependency;
	
	public Method(String name, List<Parameter> paras, Type returnType) {
		this.name = name;
		this.paras = paras;
		this.returnType = returnType;
		dependency = new ArrayList<String> ();
		
	}
}
