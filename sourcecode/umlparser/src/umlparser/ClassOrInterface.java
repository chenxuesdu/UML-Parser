package umlparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassOrInterface {
	public String name;
	public boolean isInterface;
	public String parentClass;
	public List<String> interfaces;
	public Map<String,Field> field;
	public List<Method> methods;
	public Set<String> uses;
	public ClassOrInterface (String name) {
		this.name = name;
		field = new HashMap<String, Field>();
		interfaces = new ArrayList<String> ();
		methods = new ArrayList<Method> ();
		uses = new HashSet<String> ();
	}
}
