package br.edu.ufmg.joaopaulo.crast;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.util.ASTPrinter;
import org.eclipse.core.runtime.CoreException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ufmg.joaopaulo.crast.rast.RAST;

public class App {
	private final static String PROGRAMS_FOLDER = "programs";
	
	private final static ClassLoader CLASS_LOADER = App.class.getClassLoader(); 
	
	private static IASTTranslationUnit parse(URL resourceURL) throws CoreException {
		GCCLanguage gccLanguage = GCCLanguage.getDefault();

		FileContent fileContent = FileContent.createForExternalFileLocation(resourceURL.getFile());
		
		Map<String, String> macroDefinitions = new HashMap<String, String>();
		String[] includeSearchPaths = new String[0];
		IScannerInfo si = new ScannerInfo(macroDefinitions, includeSearchPaths);
		IncludeFileContentProvider ifcp = IncludeFileContentProvider.getEmptyFilesProvider();
		IIndex idx = null;
		int options = ILanguage.OPTION_IS_SOURCE_UNIT;
		IParserLogService log = new DefaultLogService();
		return gccLanguage.getASTTranslationUnit(fileContent, si, ifcp, idx, options, log);
	}

	public static void main(String[] args) throws CoreException, JsonProcessingException {
		if (args.length == 0) {
			System.err.println("You need to pass the file name as argument.");
			return;
		}
		
		URL resourceURL = CLASS_LOADER.getResource(PROGRAMS_FOLDER + File.separator + args[0]);
		
		if (resourceURL == null) { 
			System.err.println("The argument you passed is not a valid file name");
			return;
		}
		
		RAST rast = new RAST();
		List<String> tokenList = new ArrayList<String>();

		IASTTranslationUnit translationUnit = parse(resourceURL);

		ASTPrinter.print(translationUnit);
		
		ASTVisitor visitor = new RASTVisitor(rast, translationUnit.hashCode(), tokenList);

		translationUnit.accept(visitor);

		System.out.println("------------");
		System.out.println("RAST:");
		
		rast.print();
		
		System.out.println("------------");
		System.out.println("RAST JSON:");
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		String jsonInString = mapper.writeValueAsString(rast);
		System.out.println(jsonInString);
		
		System.out.println("------------");
		System.out.println("Token List:");
		
		for (String token : tokenList) {
			System.out.println(token);
		}
	}
}
