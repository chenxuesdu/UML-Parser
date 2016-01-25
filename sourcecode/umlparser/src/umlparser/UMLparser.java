package umlparser;
/*
 * current problem
 * cannot get the real object
 * interface use interface
 * 
 * */
 
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import umlparser.ParserContext.Association;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class UMLparser {
	
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Usage: UMLparse <classpath> <output file name>");
			return;
		}
		File f = new File(args[0]);
		File output = new File(args[1]);
		if (!f.isDirectory()) {
			System.out.println("Please enter a directory");
		} else {
			UMLparser umlparser = new UMLparser();
			File[] javaFiles = f.listFiles();
			ParserContext context = new ParserContext();
			for (File javaFile : javaFiles) {
				if (!javaFile.getName().endsWith(".java"))
					continue;
				CompilationUnit cu = parseJava(javaFile);
				context.cuList.add(cu);
				new headVisitor().visit(cu, context);
			}

			for (CompilationUnit cu : context.cuList) {
				context.sb.append("[");
				new classOrInterfaceVisitor().visit(cu, context);
				new fieldVisitor().visit(cu, context);
				new constructorVisitor().visit(cu, context);
				// context.result.add(context.sb.toString());
				new methodVisitor().visit(cu, context);
				umlparser.processClassOrInterface(context);
				umlparser.processField(context);
				umlparser.processMethod(context);
				context.sb.append("]");
				context.result.add(0, context.sb.toString());
				context.sb.setLength(0);
			}
			new UMLparser().creatAssociation(context);
			// System.out.print(context.sbList.toString());
			new UMLparser().getImage(context.print(), output.toString());
		}
	}
	public void getImage(String input, String output) throws Exception {
		String image = "http://yuml.me/diagram/class/";
		URL url = new URL(image + input);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(5 * 1000);
		BufferedInputStream inStream = new BufferedInputStream(conn.getInputStream());
		byte[] data = readInputStream(inStream);
		File imageFile = new File(output);
		FileOutputStream outStream = new FileOutputStream(imageFile);
		outStream.write(data);
		outStream.close();
	}
	public static byte[] readInputStream(BufferedInputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		inStream.close();
		return outStream.toByteArray();
	}

	public void processClassOrInterface(ParserContext context) {
		ClassOrInterface current = context.currentClass;
		String name = current.name;
		boolean isInterface = current.isInterface;
		if (isInterface) {
			context.sb.append("\\<\\<interface\\>\\>;").append(name);
		} else {
			context.sb.append(name);
		}
		String parent = current.parentClass;
		if (parent != null) {
			if (context.classes.containsKey(parent)) {
				String tmp = "[" + parent + "]" + "^[" + name + "]";
				context.result.add(tmp);
			}
		}
		List<String> interfaces = current.interfaces;
		if (interfaces != null) {
			for (String l : interfaces) {
				if (context.classes.containsKey(l)) {
					String tmp = "[\\<\\<interface\\>\\>;" + l + "]" + "^-.-[" + name
							+ "]";
					context.result.add(tmp);
				}
			}

		}
	}

	public void processField(ParserContext context) {
		ClassOrInterface current = context.currentClass;
		Map<String, Field> field = current.field;
		//boolean fieldVisited = false;
		context.sb.append("|");

		for (Field f : field.values()) {
			Type fieldType = f.type;
			String type = fieldType.toString();
			
			if (fieldType instanceof ReferenceType) {
				// type is a class that has a java file
				if (context.classes.containsKey(type)) {
					Association a = new Association();
					a.start = context.currentClass.name;
					a.end = type;
					a.isMul = false;
					context.aList.add(a);
				} else {
					String refType = getType(type);
					if (type.equals(refType)) {
						//type is is a reference type that has no java file in the directory
						if (f.modifier == 2) {
							context.sb.append("-");

						} else if (f.modifier == 1) {
							context.sb.append("+");
						}
						context.sb.append(f.name).append(":").append(refType).append(";");
					} else if (!context.classes.containsKey(refType)) {
						//type is a collection or [] type that has no java file in the directory
						/*if (!fieldVisited) {
							context.sb.append("|");
							fieldVisited = true;
						}*/

						if (f.modifier == 2) {
							context.sb.append("-");

						} else if (f.modifier == 1) {
							context.sb.append("+");
						}
						context.sb.append(f.name).append(":").append(refType).append("(*);");
					} else if (context.classes.containsKey(refType)) {
						Association a = new Association();
						a.start = context.currentClass.name;
						a.end = refType;
						a.isMul = true;
						context.aList.add(a);
					}
				}
			} else if (fieldType instanceof PrimitiveType) {
				if (f.modifier == 2) {
					context.sb.append("-");

				} else if (f.modifier == 1) {
					context.sb.append("+");
				}
				context.sb.append(f.name).append(":").append(type).append(";");
			}
		}

		

	}
	public String getType(String type) {
		String result = type;
		if (type.indexOf("<") >= 0) {
			return type.substring(type.indexOf("<") + 1, type.indexOf(">") );
		} else if (type.indexOf("[") >= 0) {
			return  type.substring(0, type.indexOf("["));
		}
		return type;
	}

	public void processMethod(ParserContext context) {
		ClassOrInterface current = context.currentClass;
		List<Method> methods = current.methods;
		if (methods == null) return;
		boolean methodVisited = false;
		context.sb.append("|");
		for (Method m : methods) {
			context.sb.append("+").append(m.name);
			context.sb.append("(");

			for (Parameter para : m.paras) {
				String type = para.getType().toString();
				String name = para.getId().getName();
				context.sb.append(name).append(":");
				String refType = getType(type);
				if (!type.equals(refType)) {
					context.sb.append(refType).append("(*)").append(" ");
				} else {
					context.sb.append(type).append(" ");
				}
			}
			if (m.paras.size() > 0)
				context.sb.setLength(context.sb.length() - 1);
			context.sb.append(")");
			if (m.returnType != null) {
				context.sb.append(":").append(m.returnType);

			}
			context.sb.append(";");

		}
		if (current.isInterface) 
			return;
		for (Method m : methods) {
			if (m.dependency != null) {
				//method m has method level dependency
				for (String cla : m.dependency) {
					if (!current.uses.contains(cla)) {
						current.uses.add(cla);
						String tmp = "[" + current.name + "] uses-.->[\\<\\<interface\\>\\>;" + cla + "]";
						context.result.add(tmp);
					}
				}
			}
			List<Parameter> paras = m.paras;
		
			//check is any parameters type is interface
			for (Parameter para : paras) {
				String type = para.getType().toString();
				String refType = getType(type);
				if (!current.uses.contains(refType) && 
						context.classes.containsKey(refType) && context.classes.get(refType).isInterface) {
					//has a interface reference
					current.uses.add(refType);
					String tmp = "[";
					if (current.isInterface) {
						tmp += "\\<\\<interface\\>\\>;";
					}
					tmp += current.name + "] uses-.->[\\<\\<interface\\>\\>;" + refType + "]";
					context.result.add(tmp);
				}
			}
			if (m.returnType != null) {
				//check is return type is interface
				String returnType = m.returnType.toString();
				String refType = getType(returnType);
				if (context.classes.containsKey(refType) && context.classes.get(refType).isInterface && !current.uses.contains(refType)) {
					//return type is interface
					current.uses.add(refType);
					String tmp = "[";
					if (current.isInterface) {
						tmp += "<<interface>>;";
					}
					tmp += current.name + "] uses-.->[\\<\\<interface\\>\\>;" + refType + "]";
					context.result.add(tmp);
				}
			}		
		}
	}

	public void creatAssociation(ParserContext context) {
		List<Association> list = context.aList;
		while (!list.isEmpty()) {
			Association a1 = list.get(0);
			int index = 1;
			for (index = 1; index < list.size(); index++) {
				Association a = list.get(index);
				if (a.start.equals(a1.end) && a.end.equals(a1.start)) {
					break;
				}
			}
			if (index == list.size()) {
				String tmp = "[";
				if (context.classes.get(a1.start).isInterface) {
					tmp += "\\<\\<interface\\>\\>;";
				}
				tmp += a1.start + "]";
				if (a1.isMul) {
					tmp += "-*";
				} else {
					tmp += "-1";
				}
				tmp += "[";
				if (context.classes.get(a1.end).isInterface) {
					tmp += "\\<\\<interface\\>\\>;";
				}
				tmp += a1.end + "]";
				context.result.add(tmp);
				list.remove(0);
			} else {
				Association a = list.get(index);
				String tmp = "[";
				if (context.classes.get(a1.start).isInterface) {
					tmp += "\\<\\<interface\\>\\>;";
				}
				tmp += a1.start + "]";
				// StringBuilder sb = new StringBuilder();
				// context.sbList.add(sb);
				// sb.append("[").append(a1.start).append("]");
				if (a1.isMul && a.isMul) {
					tmp += "*-*";
				} else if (!a1.isMul && a.isMul) {
					tmp += "*-1";
				} else if (a1.isMul && !a.isMul) {
					tmp += "1-*";
				} else {
					tmp += "1-1";
				}
				tmp += "[";
				if (context.classes.get(a1.end).isInterface) {
					tmp += "\\<\\<interface\\>\\>;";
				}
				tmp += a1.end + "]";

				context.result.add(tmp);
				// System.out.println("Remove " + index);
				list.remove(index);
				list.remove(0);
			}
		}
	}

	public static CompilationUnit parseJava(File file) throws Exception {
		FileInputStream in = new FileInputStream(file);
		CompilationUnit cu;
		try {
			// parse the file
			cu = JavaParser.parse(in);
		} finally {
			in.close();
		}
		return cu;
	}

	// check field association with other classes
	private static class fieldVisitor extends VoidVisitorAdapter {
		@Override
		public void visit(FieldDeclaration n, Object arg) {
			if (n.getType() == null || (n.getModifiers() & Modifier.PUBLIC) == 0 && (n.getModifiers() & Modifier.PRIVATE) == 0)
				return;

			ParserContext context = (ParserContext) arg;
			Type type = n.getType();
			int modifier = n.getModifiers();
			for (VariableDeclarator var : n.getVariables()) {
				String name = var.getId().getName();
				Field field = new Field(modifier, name, type);
				context.currentClass.field.put(name.toLowerCase(), field);
			}
		}
	}
	
	// visit the method name, attributes, return type. Add dependency when the
	// reference is interface
	private static class methodVisitor extends VoidVisitorAdapter {
		@Override
		public void visit(MethodDeclaration n, Object arg) {
			ParserContext context = (ParserContext) arg;
			if ((n.getModifiers() & Modifier.PUBLIC) == 0) return;
			String name = n.getName();
			if (name.startsWith("get") || name.startsWith("set")) {
				String rest = name.substring(3);
					if (context.currentClass.field.containsKey(rest.toLowerCase())) {
					context.currentClass.field.get(rest.toLowerCase()).modifier = 1;
					return;
				}
			}
			Type returnType = n.getType();
			List<Parameter> paras = n.getParameters();
			
			Method method = new Method(name, paras, returnType);
			context.currentClass.methods.add(method);
			
			BlockStmt block = n.getBody();
			if (block == null) return;
			for (Node node : block.getChildrenNodes()) {
				String[] strs = node.toString().split(" ");
				String declear = strs[0];
				if (context.classes.containsKey(declear) && context.classes.get(declear).isInterface) {
					method.dependency.add(declear);
				}
			}
						
		}
	}

	// get the class name, extends class name and implements interface name
	private static class classOrInterfaceVisitor extends VoidVisitorAdapter {
		@Override
		public void visit(ClassOrInterfaceDeclaration n, Object arg) {
			ParserContext context = (ParserContext) arg;
			String name = n.getName();
			ClassOrInterface ci = context.classes.get(name);
			
			context.currentClass = ci;
			// get the extends parent class
			if (n.getExtends() != null) {
				ci.parentClass = n.getExtends().get(0).getName();
			}
			// get the implements interface
			if (n.getImplements() != null) {
				List<ClassOrInterfaceType> list = n.getImplements();
				for (ClassOrInterfaceType l : list) {
					context.currentClass.interfaces.add(l.getName());
				}
			}
		}

	}
	private static class constructorVisitor extends VoidVisitorAdapter {
		@Override
		public void visit(ConstructorDeclaration n, Object arg) {
			ParserContext context = (ParserContext) arg;
				if (n.getName() != null) {
				String name = n.getName();
				List<Parameter> paras = n.getParameters();
				Method con = new Method(name, paras, null);
				context.currentClass.methods.add(con);
			}
		}
	}

	// get the class Name and isInterface, store them in a set for reference
	private static class headVisitor extends VoidVisitorAdapter {
		@Override
		public void visit(ClassOrInterfaceDeclaration n, Object arg) {
			ParserContext context = (ParserContext) arg;
			String name = n.getName();
			boolean isInterface = n.isInterface();
			ClassOrInterface ci = new ClassOrInterface(name);
			ci.isInterface = isInterface;
			if (!context.classes.containsKey(name)) {
				context.classes.put(name, ci);
			}
			/*
			 * if (!context.classMap.containsKey(name)) {
			 * context.classMap.put(name, isInterface); }
			 */
		}
	}
}
