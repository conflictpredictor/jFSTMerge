package br.ufpe.cin.mergers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.merge.MergeAlgorithm;
import org.eclipse.jgit.merge.MergeFormatter;
import org.eclipse.jgit.merge.MergeResult;

import br.ufpe.cin.exceptions.ExceptionUtils;
import br.ufpe.cin.exceptions.TextualMergeException;
import br.ufpe.cin.files.FilesManager;
import de.ovgu.cide.fstgen.ast.FSTTerminal;

/**
 * Represents unstructured, linebased, textual merge.
 * @author Guilherme
 */
public final class TextualMerge {

	/**
	 * Three-way unstructured merge of three given files.
	 * @param left
	 * @param base
	 * @param right
	 * @param ignoreWhiteSpaces to avoid false positives conflicts due to different spacings.
	 * @return string representing merge result (might be null in case of errors).
	 * @throws TextualMergeException 
	 */
	public static String merge(File left, File base, File right, boolean ignoreWhiteSpaces) throws TextualMergeException{
		/* this commented code is an alternative to call unstructured merge by command line 		
		 * String mergeCommand = ""; 
			if(System.getProperty("os.name").contains("Windows")){
				mergeCommand = "C:/KDiff3/bin/diff3.exe -m -E " + "\"" 
						+ left.getPath() + "\"" + " " + "\"" 
						+ base.getPath() + "\"" + " " + "\"" 
						+ right.getPath()+ "\"";
			} else {
				mergeCommand = "git merge-file -q -p " 
						+ left.getPath() + " " 
						+ base.getPath() + " " 
						+ right.getPath();// + " > " + fileVar1.getName() + "_output";
			}
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(mergeCommand);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			textualMergeResult = reader.lines().collect(Collectors.joining("\n"));*/

		String textualMergeResult = null;
		//we treat invalid files as empty files 
		String leftContent = ((left == null || !left.exists()) ? "" : FilesManager.readFileContent(left));
		String baseContent = ((base == null || !base.exists()) ? "" : FilesManager.readFileContent(base));
		String rightContent= ((right== null || !right.exists())? "" : FilesManager.readFileContent(right));
		textualMergeResult = merge(leftContent,baseContent,rightContent,ignoreWhiteSpaces);
		return textualMergeResult;
	}


	/**
	 * Merges textually three strings.
	 * @param leftContent
	 * @param baseContent
	 * @param rightContent
	 * @param ignoreWhiteSpaces to avoid false positives conflicts due to different spacings.
	 * @return merged string.
	 * @throws TextualMergeException 
	 */
	public static String merge(String leftContent, String baseContent, String rightContent, boolean ignoreWhiteSpaces) throws TextualMergeException{
		String textualMergeResult = null;
		try{
			RawTextComparator textComparator = ((ignoreWhiteSpaces) ? RawTextComparator.WS_IGNORE_ALL : RawTextComparator.DEFAULT);
			@SuppressWarnings("rawtypes") MergeResult mergeCommand = new MergeAlgorithm().merge(textComparator,
					new RawText(Constants.encode(baseContent)), 
					new RawText(Constants.encode(leftContent)), 
					new RawText(Constants.encode(rightContent))
					);		
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			(new MergeFormatter()).formatMerge(output, mergeCommand, "BASE", "MINE", "YOURS", Constants.CHARACTER_ENCODING);
			textualMergeResult = new String(output.toByteArray(), Constants.CHARACTER_ENCODING);
		}catch(Exception e){
			throw new TextualMergeException(ExceptionUtils.getCauseMessage(e), leftContent,baseContent,rightContent);
		}
		return textualMergeResult;
	}
	
	//#conflictsAnalyzer
	/*
	 * this commented code uses the gnu diff3 which is not supported on windows plaftorm. 
	 * as a solution, the uncommented  mergediff3 method calls git merge*/
	/*public static String mergeDiff3(String leftContent, String baseContent, String rightContent, FSTTerminal node) throws TextualMergeException{
		String textualMergeResult = "";

		try {
			long time = System.currentTimeMillis();
			File tmpDir = new File(System.getProperty("user.dir") + File.separator + "fstmerge_tmp"+time);
			tmpDir.mkdir();
			File fileVar1 = File.createTempFile("fstmerge_var1_", "", tmpDir);
			File fileBase = File.createTempFile("fstmerge_base_", "", tmpDir);
			File fileVar2 = File.createTempFile("fstmerge_var2_", "", tmpDir);
			
			BufferedWriter writerVar1 = new BufferedWriter(new FileWriter(fileVar1));
			if(leftContent.length() == 0){
				writerVar1.write(leftContent);
			}else{
				writerVar1.write(leftContent + "\n");
			}
			writerVar1.close();

			BufferedWriter writerBase = new BufferedWriter(new FileWriter(fileBase));
			if(baseContent.length() == 0){
				writerBase.write(baseContent);
			}else {
				writerBase.write(baseContent + "\n");
			}	
			writerBase.close();

			BufferedWriter writerVar2 = new BufferedWriter(new FileWriter(fileVar2));
			if(rightContent.length() == 0){
				writerVar2.write(rightContent);
			}else {
				writerVar2.write(rightContent + "\n");
			}
			writerVar2.close();
			
			String mergeCmd = ""; 
			if(System.getProperty("os.name").contains("Windows")){
				mergeCmd = "C:\\Programme\\cygwin\\bin\\merge.exe -q -p " + "\"" + fileVar1.getPath() + "\"" + " " + "\"" + fileBase.getPath() + "\"" + " " + "\"" + fileVar2.getPath() + "\"";// + " > " + fileVar1.getName() + "_output";
			}else{
				mergeCmd = "diff3 --merge -E " + fileVar1.getPath() + " " + fileBase.getPath() + " " + fileVar2.getPath();// + " > " + fileVar1.getName() + "_output";
			}

			Runtime run = Runtime.getRuntime();
			Process pr = run.exec(mergeCmd);
			
			
			BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = "";
			while ((line=buf.readLine())!=null) {
				textualMergeResult += line + "\n";
			}
			pr.getInputStream().close();
			
			if(textualMergeResult.contains(SemistructuredMerge.DIFF3MERGE_SEPARATOR)){
				mergeCmd = "diff3 --merge " + fileVar1.getPath() + " " + fileBase.getPath() + " " + fileVar2.getPath();// + " > " + fileVar1.getName() + "_output";
				run = Runtime.getRuntime();
				pr = run.exec(mergeCmd);

				buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				line = "";
				textualMergeResult = "";
				while ((line=buf.readLine())!=null) {
					textualMergeResult += line + "\n";
				}
				pr.getInputStream().close();

			}
			//conflictPredictor
			else{
				String[] tokens = {leftContent, baseContent, rightContent};
				if(isConflictPredictor(node, tokens)){
					textualMergeResult = node.getBody();
				}
			}
			//conflictPredictor
			
			buf = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
			while ((line=buf.readLine())!=null) {
				System.err.println(line);
			}
			pr.getErrorStream().close();
			pr.getOutputStream().close();

			fileVar1.delete();
			fileBase.delete();
			fileVar2.delete();
			tmpDir.delete();


		} catch (IOException e) {
			e.printStackTrace();
		}

		return textualMergeResult;
	}*/
	
	/*This method first executes diff3 using jgit api
	 *ignoring spaces. then, if the code conflicts,
	 * it calls gitmerge directly from command line
	 * to get the code base version
	 * */
	public static String mergeGit(String leftContent, String baseContent, String rightContent, FSTTerminal node) {
		String textualMergeResult = "";
		try {
			//call jgit merge
			textualMergeResult = merge(leftContent, baseContent, rightContent, true);
			if(textualMergeResult.contains(SemistructuredMerge.DIFF3MERGE_SEPARATOR)) {
				//create temp files
				File[] files = TextualMerge.createTempFiles(leftContent, baseContent, rightContent); 
				//call git merge
				int result = TextualMerge.callGitMerge(files);
				//read left file content
				textualMergeResult = TextualMerge.readLeftFile(files[0]);
				// delete temp files
				TextualMerge.deleteTempFiles(files);
			}
		} catch (TextualMergeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] tokens = {leftContent, baseContent, rightContent};
		if(!textualMergeResult.contains(SemistructuredMerge.DIFF3MERGE_SEPARATOR) && isConflictPredictor(node, tokens)) {
			textualMergeResult = node.getBody();
		}	

		return textualMergeResult;
	}
	
	public static File[] createTempFiles(String leftContent, String baseContent, String rightContent) {
		File [] result = {null, null, null};
		try {
			long time = System.currentTimeMillis();
			File tmpDir = new File(System.getProperty("user.dir") + File.separator + "fstmerge_tmp"+time);
			tmpDir.mkdir();
			File fileVar1 = File.createTempFile("fstmerge_var1_", "", tmpDir);
			File fileBase = File.createTempFile("fstmerge_base_", "", tmpDir);
			File fileVar2 = File.createTempFile("fstmerge_var2_", "", tmpDir);
			
			BufferedWriter writerVar1 = new BufferedWriter(new FileWriter(fileVar1));
			if(leftContent.length() == 0){
				writerVar1.write(leftContent);
			}else{
				writerVar1.write(leftContent + "\n");
			}
			writerVar1.close();

			BufferedWriter writerBase = new BufferedWriter(new FileWriter(fileBase));
			if(baseContent.length() == 0){
				writerBase.write(baseContent);
			}else {
				writerBase.write(baseContent + "\n");
			}	
			writerBase.close();
			
			BufferedWriter writerVar2 = new BufferedWriter(new FileWriter(fileVar2));
			if(rightContent.length() == 0){
				writerVar2.write(rightContent);
			}else {
				writerVar2.write(rightContent + "\n");
			}
			writerVar2.close();
			
			result[0] = fileVar1;
			result[1] = fileBase; 
			result[2] = fileVar2;
		}catch (IOException e) {
			e.printStackTrace();
		}	
		
		return result;
	}
	
	public static int callGitMerge(File[] files) {
		int result = -1;
		String mergeCmd = "git merge-file --diff3 \"" + files[0].getPath() + "\" \"" +  files[1].getPath() + "\" \"" + files[2].getPath() + "\"";
		Runtime run = Runtime.getRuntime();
		try {
			Process pr = run.exec(mergeCmd);
			result = pr.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String readLeftFile(File file) {
		String leftContent = "";
		BufferedReader br = null;
		FileReader fr = null;

		try {

			fr = new FileReader(file);
			br = new BufferedReader(fr);

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				leftContent += sCurrentLine + "\n";
			}
			//leftContent = leftContent.substring(0, leftContent.length()-1);
			fr.close();
			br.close();

		} catch (IOException e) {

			e.printStackTrace();

		}
		return leftContent;
	}
	
	public static void deleteTempFiles(File[] files) {
		File tempDir = files[0].getParentFile();
		files[0].delete();
		files[1].delete();
		files[2].delete();
		tempDir.delete();
	}
	
	//#conflictsAnalyzer
	
	//conflictPredictor
		/*the node represents a conflict predictor in the following cases:
		 * 1- it is a method or constructor and at least one version was edited
		 * 2- it is a class field, BOTH versions were edited, and the base 
		 * version is not empty */
		private static boolean isConflictPredictor(FSTTerminal node, String[] tokens){
			boolean result = false;
			if((isMethodOrConstructor(node.getType()) && atLeastOneVersionWasEdited(tokens)) ||
					(node.getType().equals("FieldDecl") && bothVersionsWereDifferentlyEdited(tokens) 
							&& !tokens[1].equals("")) ){
				result = true;
			}
			return result;
		}
		
		public static boolean isMethodOrConstructor(String type){
			boolean result = type.equals("MethodDecl") || type.equals("ConstructorDecl");	
			return result;
		}
		
		/* #conflictsAnalyzer 
		 * returns true if at least one version (left or right) differs from base*/
		private static boolean atLeastOneVersionWasEdited(String[] tokens){
			boolean result = false;
			if( !tokens[0].equals("") && !tokens[2].equals("") && !tokens[1].equals("") &&
					( !tokens[0].equals(tokens[1]) || !tokens[2].equals(tokens[1]) ) &&
					!tokens[0].equals(tokens[2])){
				result = true;
			}
			return result;
		}
		
		private static boolean bothVersionsWereDifferentlyEdited(String[] tokens){
			boolean result = false;
			if( !tokens[0].equals("") && !tokens[2].equals("") 
					&& !tokens[1].equals("") &&  !tokens[0].equals(tokens[1]) 
					&& !tokens[2].equals(tokens[1]) && !tokens[2].equals(tokens[0])){
				result = true;
			}
			return result;
		}

		//conflictPredictor

}
