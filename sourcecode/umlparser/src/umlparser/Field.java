package umlparser;

import com.github.javaparser.ast.type.Type;

public class Field {
	public int modifier;//1 for private; 2 for public
	public String name;
	public Type type;
	public Type key;
	public Field (int modifier, String name, Type type) {
		this.modifier = modifier;
		this.name = name;
		this.type = type;
	}
	
}
