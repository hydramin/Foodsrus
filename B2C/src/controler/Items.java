package controler;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.Engine;
import model.ItemBean;

/**
 * Servlet implementation class Items
 */
@WebServlet("/Items.do")
public class Items extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Items() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/* 
		 * gets list of items and gets category id from the dropdown
		 * sends both as parameters when serving items.jspx
		 * in items.jspx we only display the items in the category 
		 * that was selected using the cadID we got from the dropdown 
		 * */
		Engine engine = Engine.getInstance();
		List<ItemBean> list;
		String catID=request.getParameter("categories") ;
		if (request.getParameter("categories").equals("Categories"))
			response.sendRedirect("Category.do");
		else {
			try {
					list = engine.doItem("");
					request.setAttribute("items", list);
					request.setAttribute("catID", catID);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			/*call services of the Engine. pass the search terms to the engine. the method is called doSearch, doItem*/
			/*do item returns a list of ItemBean objects*/
			this.getServletContext().getRequestDispatcher("/pages/items.jspx").forward(request, response);			
		}
		
		}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}