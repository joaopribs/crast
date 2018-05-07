package br.edu.ufmg.joaopaulo.crast;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTUnaryExpression;

import br.edu.ufmg.joaopaulo.crast.rast.Location;
import br.edu.ufmg.joaopaulo.crast.rast.Node;
import br.edu.ufmg.joaopaulo.crast.rast.Parameter;
import br.edu.ufmg.joaopaulo.crast.rast.ParentNode;
import br.edu.ufmg.joaopaulo.crast.rast.RAST;
import br.edu.ufmg.joaopaulo.crast.rast.Relationship;

public class RASTVisitor extends ASTGenericVisitor {

	private Long id = 1L;
	private Node currentNode;
	private Relationship currentRelationship;
	private Map<Integer, Node> nodesHash = new HashMap<Integer, Node>();
	private RAST rast;
	private Node root;
	private List<String> tokenList;
	private String waitingName;
	private String waitingBodyLocation;
	private String waitingType;

	public RASTVisitor(RAST rast, Integer hashCode, List<String> tokenList) {
		super(true);
		this.tokenList = tokenList;
		this.rast = rast;
	}
	
	@Override
	protected int genericVisit(IASTNode iastNode) {
		IToken token = null;
		try {
			token = iastNode.getSyntax();
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (ExpansionOverlapsBoundaryException e) {
			e.printStackTrace();
		}
		
		if (this.tokenList.isEmpty() && token != null) {
			while (token != null) {
				this.tokenList.add(token.getImage());
				token = token.getNext();
			}
		}
		
		if (!this.shouldSkip(iastNode)) {
			if (iastNode instanceof CASTFunctionCallExpression) {
				Node parentNode = (Node) this.getRASTParent(iastNode);
				
				this.currentRelationship = new Relationship();
				this.currentRelationship.setN1(parentNode.getId());
				this.currentRelationship.setType("USE");
				
				this.waitingName = "Relationship";
			}
			else if (iastNode instanceof CASTParameterDeclaration) {
				this.waitingName = "Parameter";
				this.waitingType = "Parameter";
			}
			else if (iastNode instanceof CASTCompoundStatement) {
				if (this.waitingBodyLocation.equals("FunctionDeclaration")) {
					int offset = ((CASTCompoundStatement) iastNode).getOffset();
					int length = ((CASTCompoundStatement) iastNode).getLength();
					
					Location location = this.currentNode.getLocation();
					location.setBodyBegin(offset);
					location.setBodyEnd(offset + length);
					
					this.waitingBodyLocation = null;
				}
			}
			else if (iastNode instanceof CASTSimpleDeclSpecifier) {
				if (this.waitingType != null && this.waitingType.equals("Parameter")) {
					String type = this.getType((CASTSimpleDeclSpecifier) iastNode);
					
					String localName = this.currentNode.getLocalName();
					
					int closingParenthesisIndex = localName.indexOf(")");
					
					if (localName.charAt(closingParenthesisIndex - 1) != '(') {
						type = ", " + type;
					}
					
					StringBuilder newLocalNameBuilder = new StringBuilder();
					newLocalNameBuilder.append(localName.substring(0, closingParenthesisIndex));
					newLocalNameBuilder.append(type);
					newLocalNameBuilder.append(")");
					
					this.currentNode.setLocalName(newLocalNameBuilder.toString());
					
					this.waitingType = null;
				}
			}
			else if (iastNode instanceof IASTName) {
				if (this.waitingName != null) {
					String name = ((IASTName) iastNode).toString();
					
					if (this.waitingName.equals("FunctionDeclaration")) {
						this.currentNode.setSimpleName(name);
						this.currentNode.setLocalName(name + "()");	
					}
					else if (this.waitingName.equals("Parameter")) {
						Parameter parameter = new Parameter();
						parameter.setName(name);
						
						Node parentNode = (Node) this.getRASTParent(iastNode);
						parentNode.getParameters().add(parameter);
					}
					else if (this.waitingName.equals("Relationship")) {
						Node n2 = this.getBySimpleName(name);
						if (n2 != null) {
							if (!this.rast.hasRelationship(
									this.currentRelationship.getN1(), 
									n2.getId(), 
									"USE")) {
								this.currentRelationship.setN2(n2.getId());
								this.rast.getRelationships().add(this.currentRelationship);								
							}
						}
						
						this.currentRelationship = null;
					}
					
					this.waitingName = null;
				}
			}
			else {
				if (iastNode instanceof CASTFunctionDefinition) {
					this.waitingName = "FunctionDeclaration";
					this.waitingBodyLocation = "FunctionDeclaration";
				}
				
				Node node = this.createNode((ASTNode) iastNode);

				ParentNode parentNode = this.getRASTParent(iastNode);
				
				parentNode.getNodes().add(node);

				this.nodesHash.put(iastNode.hashCode(), node);
				
				this.currentNode = node;
				
				if (iastNode instanceof CASTTranslationUnit) {
					this.root = node;
				}
			}				
		}

		return PROCESS_CONTINUE;
	}
	
	private Node createNode(ASTNode astNode) {
		Node rastNode = new Node();
		
		rastNode.setType(this.getRASTType(astNode));
		
		int offset = astNode.getOffset();
		int length = astNode.getLength();
		String[] filenameParts = astNode.getContainingFilename().split(File.separator);
		String filename = filenameParts[filenameParts.length - 1];

		Location location = new Location();
		location.setBegin(offset);
		location.setEnd(offset + length);
		location.setFile(filename);
		
		if (astNode instanceof CASTTranslationUnit) {
			location.setBodyBegin(offset);
			location.setBodyEnd(offset + length);
		}
		
		rastNode.setLocation(location);

		rastNode.setId(this.id);
		
		this.id++;
		
		return rastNode;
	}
	
	private String getRASTType(ASTNode astNode) {
		if (astNode instanceof CASTTranslationUnit) {
			return "Program";
		}
		else if (astNode instanceof CASTFunctionDefinition) {
			return "FunctionDeclaration";
		}
		else if (astNode instanceof CASTParameterDeclaration) {
			return "Parameter";
		}
		
		return astNode.getClass().getName();
	}
	
	private boolean shouldSkip(IASTNode iastNode) {
		return iastNode instanceof CASTFunctionDeclarator
				|| iastNode instanceof CASTExpressionStatement
				|| iastNode instanceof CASTIdExpression 
				|| iastNode instanceof CASTLiteralExpression
				|| iastNode instanceof CASTDeclarator
				|| iastNode instanceof CASTReturnStatement
				|| iastNode instanceof CASTDeclarationStatement
				|| iastNode instanceof CASTSimpleDeclaration
				|| iastNode instanceof CASTUnaryExpression
				|| iastNode instanceof CASTBinaryExpression;
	}
	
	private ParentNode getRASTParent(IASTNode iastNode) {
		if (iastNode instanceof CASTTranslationUnit) {
			return this.rast;
		}
		
		Node rastParent = this.nodesHash.get(iastNode.getParent().hashCode());
		
		if (rastParent == null) {
			rastParent = this.currentNode;
		}
		
		return rastParent;
	}
	
	private Node getBySimpleName(String simpleName) {
		return this.root.searchBySimpleName(simpleName);
	}
	
	private String getType(IASTSimpleDeclSpecifier node) {
		int type = node.getType();
		
		if (type == IASTSimpleDeclSpecifier.t_auto) { 
			return "auto";
		}
		else if (type == IASTSimpleDeclSpecifier.t_bool) {
			return "Bool";
		}
		else if (type == IASTSimpleDeclSpecifier.t_char) {
			return "char";
		}
		else if (type == IASTSimpleDeclSpecifier.t_char16_t) {
			return "char16_t";
		}
		else if (type == IASTSimpleDeclSpecifier.t_char32_t) {
			return "char32_t";
		}
		else if (type == IASTSimpleDeclSpecifier.t_decltype) {
			return "decltype";
		}
		else if (type == IASTSimpleDeclSpecifier.t_double) {
			return "double";
		}
		else if (type == IASTSimpleDeclSpecifier.t_float) {
			return "float";
		}
		else if (type == IASTSimpleDeclSpecifier.t_int) {
			return "int";
		}
		else if (type == IASTSimpleDeclSpecifier.t_typeof) {
			return "typeof";
		}
		else if (type == IASTSimpleDeclSpecifier.t_void) {
			return "void";
		}
		else if (type == IASTSimpleDeclSpecifier.t_wchar_t) {
			return "wchar_t";
		}
		
		return null;
	}
	
}
