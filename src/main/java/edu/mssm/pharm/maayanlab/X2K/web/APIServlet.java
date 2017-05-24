package edu.mssm.pharm.maayanlab.X2K.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import edu.mssm.pharm.maayanlab.ChEA.ChEA;
import edu.mssm.pharm.maayanlab.Genes2Networks.Genes2Networks;
import edu.mssm.pharm.maayanlab.Genes2Networks.NetworkNode;
import edu.mssm.pharm.maayanlab.KEA.KEA;
import edu.mssm.pharm.maayanlab.X2K.enrichment.Network;
import edu.mssm.pharm.maayanlab.X2K.enrichment.X2K;
import edu.mssm.pharm.maayanlab.common.web.JSONify;
import edu.mssm.pharm.maayanlab.common.web.PartReader;


@WebServlet(urlPatterns = { "/api" })
@MultipartConfig
public class APIServlet extends HttpServlet {

	private static final long serialVersionUID = -7824900778605131322L;
	private String chEA;
	private String g2n;
	private String kEA;
	private String x2k;
	
	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Blank page		
    }
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	    Part geneChunk = req.getPart("text-genes");
	    ArrayList<String> textGenes = PartReader.readLines(geneChunk);

		System.out.println("POST request - ChEA");
		if (textGenes.size() > 0) {
	        System.out.println("Using text genes:");
	        System.out.println(textGenes);
	        setChEA(runChEA(textGenes, req, resp));
	    } else {
	        System.out.println("no lists received - error");
	    }

	    System.out.println("POST request - G2N");
	    if(textGenes.size() > 0){
	        setG2n(runG2N(textGenes, req, resp));
	    }
	    else{
	        System.out.println("no lists received - error");
	    }

	    System.out.println("POST request - KEA");
	    if(textGenes.size() > 0){ //as text
	        setkEA(runKEA(textGenes, req, resp));
	    }
	    else{
	        System.out.println("no lists received - error");
	    }

	    System.out.println("POST request - X2K");
	    if(textGenes.size() > 0){
	        setX2k(enrichList(textGenes, req, resp));
	    }
	    else{
	        System.out.println("no lists received - error");
	    }
	    
	    System.out.println("Result");
        JSONify json = Context.getJSONConverter();
        json.add("ChEA", chEA);
        json.add("G2N", g2n);
        json.add("KEA", kEA);
        json.add("X2K", x2k);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().print(json);
	}
	
	// ChEA procedures
    public static String runChEA(ArrayList<String> inputList, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Run enrichment
        ChEA app = new ChEA();
        System.out.println(app);
        System.out.println(ChEA.SORT_BY);
        System.out.println(req.getParameter("sort_tfs_by"));
        System.out.println(ChEA.INCLUDED_ORGANISMS);
        System.out.println(req.getParameter("included_organisms"));
        System.out.println(ChEA.BACKGROUND_DATABASE);
        System.out.println(req.getParameter("included_database"));
        app.setSetting(ChEA.SORT_BY, req.getParameter("sort_tfs_by"));
        app.setSetting(ChEA.INCLUDED_ORGANISMS, req.getParameter("included_organisms"));
        app.setSetting(ChEA.BACKGROUND_DATABASE, req.getParameter("included_database"));
        app.run(inputList);

        // Write app to session
        HttpSession httpSession = req.getSession();
        httpSession.setAttribute("app", app);

        // Write output
        JSONify json = Context.getJSONConverter();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        json.add("type", "ChEA");
        json.add("tfs", app.getTopRanked(Integer.parseInt(req.getParameter("number_of_results"))));
        json.add(ChEA.SORT_BY, req.getParameter("sort_tfs_by"));
        return json.toString();
    }

    // G2N procedures
    
    public static Network makeNetwork(Genes2Networks app){
        Network network = new Network();

        HashSet<NetworkNode> networkSet = app.getNetworkSet();
        for (NetworkNode node : networkSet) {
            network.addNode(Network.nodeTypes.networkNode, node, node.getName().split("-")[0]);
        }

        for (NetworkNode node : networkSet) {
            HashSet<NetworkNode> neighbors = node.getNeighbors();
            for (NetworkNode neighbor : neighbors)
                if (network.contains(neighbor.getName()))
                    network.addInteraction(node.getName().split("-")[0], neighbor.getName().split("-")[0]);
        }

        return network;
    }

    public static String runG2N(ArrayList<String> inputList, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Run enrichment
        Genes2Networks app = new Genes2Networks();
        app.setSetting(Genes2Networks.PATH_LENGTH, req.getParameter("path_length"));
        app.setSetting(Genes2Networks.MAXIMUM_NUMBER_OF_EDGES, req.getParameter("max_number_of_interactions_per_protein"));
        app.setSetting(Genes2Networks.MAXIMUM_NUMBER_OF_INTERACTIONS, req.getParameter("max_number_of_interactions_per_article"));
        app.setSetting(Genes2Networks.MINIMUM_NUMBER_OF_ARTICLES, req.getParameter("min_number_of_articles_supporting_interaction"));
        app.setSetting(Genes2Networks.ENABLE_BIOCARTA, req.getParameter("biocarta"));
        app.setSetting(Genes2Networks.ENABLE_BIOGRID, req.getParameter("biogrid"));
        app.setSetting(Genes2Networks.ENABLE_DIP, req.getParameter("dip"));
        app.setSetting(Genes2Networks.ENABLE_INNATEDB, req.getParameter("innatedb"));
        app.setSetting(Genes2Networks.ENABLE_INTACT, req.getParameter("intact"));
        app.setSetting(Genes2Networks.ENABLE_KEGG, req.getParameter("kegg"));
        app.setSetting(Genes2Networks.ENABLE_MINT, req.getParameter("mint"));
        app.setSetting(Genes2Networks.ENABLE_PPID, req.getParameter("ppid"));
        app.setSetting(Genes2Networks.ENABLE_SNAVI, req.getParameter("snavi"));
        app.run(inputList);
        // Write app to session
        HttpSession httpSession = req.getSession();
        httpSession.setAttribute("app", app);
        // Write output
        JSONify json = Context.getJSONConverter();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        json.add("type","G2N");
        json.add("network",makeNetwork(app));
        json.add("input_list",inputList);
        
        return json.toString();
    }

    // KEA procedures
    public static String runKEA(ArrayList<String> inputList, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Run enrichment
        KEA app = new KEA();
        app. setSetting(KEA.SORT_BY, req.getParameter("sort_kinases_by"));
        app.run(inputList);

        // Write app to session - to be investigated
        HttpSession httpSession = req.getSession();
        httpSession.setAttribute("app", app);

        // Write output
        JSONify json = Context.getJSONConverter();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        json.add("type","KEA");
        json.add("kinases", app.getTopRanked(Integer.parseInt(req.getParameter("number_of_results"))));
        json.add(KEA.SORT_BY, req.getParameter("sort_kinases_by"));
        return json.toString();
    }
    
    // X2K procedures
	private static final HashMap<String, String[]> availableSettings = new HashMap<String, String[]>();

	static {
		availableSettings.put(ChEA.SORT_BY, new String[] { ChEA.PVALUE, ChEA.RANK, ChEA.COMBINED_SCORE });
		availableSettings.put(ChEA.INCLUDED_ORGANISMS, new String[] { ChEA.MOUSE_ONLY, ChEA.HUMAN_ONLY, ChEA.BOTH });
		availableSettings.put(ChEA.BACKGROUND_DATABASE, new String[] { ChEA.CHIPX, ChEA.PWM, ChEA.PWM_GB,
																	   ChEA.CHEA_2015, ChEA.TRANS_JASP,
																	   ChEA.CONSENSUS, ChEA.ENCODE_2015});
		availableSettings.put(Genes2Networks.PATH_LENGTH, new String[] { "1", "4", "7", "10", "13", "16" });
		availableSettings.put(Genes2Networks.MAXIMUM_NUMBER_OF_EDGES, new String[] { "0", "1", "4", "5",  "8" });
		availableSettings.put(Genes2Networks.MAXIMUM_NUMBER_OF_INTERACTIONS, new String[] { "0", "1", "4", "7", "10" });
		availableSettings.put(Genes2Networks.MINIMUM_NUMBER_OF_ARTICLES, new String[] { "0", "1", "4", "7", "10" });
		availableSettings.put(Genes2Networks.ENABLE_BIOCARTA, new String[] { "true", "false" });
		availableSettings.put(Genes2Networks.ENABLE_BIOGRID, new String[] { "true", "false" });
		availableSettings.put(Genes2Networks.ENABLE_DIP, new String[] { "true", "false" });
		availableSettings.put(Genes2Networks.ENABLE_INNATEDB, new String[] { "true", "false" });
		availableSettings.put(Genes2Networks.ENABLE_INTACT, new String[] { "true", "false" });
		availableSettings.put(Genes2Networks.ENABLE_KEGG, new String[] { "true", "false" });
		availableSettings.put(Genes2Networks.ENABLE_MINT, new String[] { "true", "false" });
		availableSettings.put(Genes2Networks.ENABLE_PPID, new String[] { "true", "false" });
		availableSettings.put(Genes2Networks.ENABLE_SNAVI, new String[] { "true", "false" });
		availableSettings.put(KEA.SORT_BY, new String[] { KEA.PVALUE, KEA.RANK, KEA.COMBINED_SCORE });
		availableSettings.put(X2K.MINIMUM_NETWORK_SIZE, new String[] { "1", "4","7",  "10", "13"});
		// availableSettings.put(X2K.NUMBER_OF_TOP_TFS, new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" });
		availableSettings.put(X2K.NUMBER_OF_TOP_TFS, new String[] { "15" });
		//availableSettings.put(X2K.NUMBER_OF_TOP_KINASES, new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" });
	}

	public static String enrichList(ArrayList<String> inputList, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Run enrichment
		X2K app = new X2K();

		readAndSetSettings(req, app);
		app.run(inputList);
		

		// Write app to session
		HttpSession httpSession = req.getSession();
		httpSession.setAttribute("app", app);

		// Write output
		JSONify json = Context.getJSONConverter();
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");

		Network network = app.webNetwork();

		// System.out.println(network.interactions.size());
		json.add("type", "X2K");
		json.add("network", network);
		json.add("transcriptionFactors", app.getRankedTFs());
		json.add("kinases", app.getRankedKinases());
		// json.write(resp.getWriter());
		return json.toString();
	}

	private static void readAndSetSettings(HttpServletRequest req, X2K app) {
		for (String setting : availableSettings.keySet()){
		 if(req.getParameter(setting) != null){
			 app.setSetting(setting, req.getParameter(setting));
		 	}
		 }
	}
	
	public static HashMap<String, String[]> getAvailablesettings() {
		return availableSettings;
	}
	
	// Fields getters and setters
	public String getChEA() {
		return chEA;
	}

	public void setChEA(String chEA) {
		this.chEA = chEA;
	}

	public String getG2n() {
		return g2n;
	}

	public void setG2n(String g2n) {
		this.g2n = g2n;
	}

	public String getkEA() {
		return kEA;
	}

	public void setkEA(String kEA) {
		this.kEA = kEA;
	}

	public String getX2k() {
		return x2k;
	}

	public void setX2k(String x2k) {
		this.x2k = x2k;
	}
}