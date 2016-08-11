package edu.mssm.pharm.maayanlab.X2K.web;


import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import edu.mssm.pharm.maayanlab.KEA.KEA;
import edu.mssm.pharm.maayanlab.common.web.JSONify;
import edu.mssm.pharm.maayanlab.common.web.PartReader;


@WebServlet(urlPatterns = { "/KEA" })
@MultipartConfig
public class KEAServlet extends HttpServlet {

    //idk what's going on here
    private static final long serialVersionUID = 6594466236752893591L;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("doing get request - KEA");
    }

    public static void runKEA(ArrayList<String> inputList, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Run enrichment
        KEA app = new KEA();
        app. setSetting(KEA.SORT_BY, req.getParameter(KEA.SORT_BY));
        app.run(inputList);

        // Write app to session - to be investigated
        HttpSession httpSession = req.getSession();
        httpSession.setAttribute("app", app);

        // Write output
        JSONify json = Context.getJSONConverter();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        json.add("type","KEA");
        json.add("kinases", app.getTopRanked(10));
        json.add(KEA.SORT_BY, req.getParameter(KEA.SORT_BY));
        req.setAttribute("json",json);
        req.getRequestDispatcher("/ChEA_and_KEA.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("doing post request - KEA");
        Part fileChunk = req.getPart("geneList");
        ArrayList<String> inputList = PartReader.readLines(fileChunk);

        Part geneChunk = req.getPart("textGenes");
        ArrayList<String> textGenes = PartReader.readLines(geneChunk);

        //handle both possible types of input
        if(inputList.size() > 0){ //from file selection
            runKEA(inputList, req, resp);
        }
        else if(textGenes.size() > 0){ //as text
            runKEA(textGenes, req, resp);
        }
        else{
            System.out.println("no lists received - error");
        }
    }
}
