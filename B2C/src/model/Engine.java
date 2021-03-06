package model;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import Orders.CustomerType;
import Orders.ItemType;
import Orders.ItemsType;
import Orders.ObjectFactory;
import Orders.OrderType;

public class Engine {
	private final String poPath = System.getProperty("user.dir") + "/B2C/PO/";
	private static Engine instance = null;
	private final double HST = 0.13;
	private static ObjectFactory factory = new ObjectFactory();

	private Engine() {

	}

	/**
	 * @Description creates and delivers a singleton instance
	 * @return returns the singleton Engine object
	 */
	public synchronized static Engine getInstance() {
		if (instance == null)
			instance = new Engine();
		return instance;
	}

	/**
	 * @Description It takes in a search term and searches the database through the
	 *              ItemDAO
	 * @param searchTerm
	 * @return a List<ItemBean> containing all items that match the search term
	 * @throws Exception
	 */
	public List<ItemBean> doItem(String searchTerm, String flag) throws Exception {
		/*
		 * split the search term by using regex loopthrough the array and pass a single
		 * search term to the DAO get the list from the DAO and add the contents to the
		 * total list return the total list
		 */
		/*
		 * if flag is not null, then the search bar is used with multiple search words
		 */
		String bySearchTerm = "bySearchTerm";
		if (bySearchTerm.equals(flag)) {
			String split[] = searchTerm.split("[\\s+]|[,+]");
			ArrayList<String> terms = new ArrayList<>();
			for (String term : split) {
				if (!term.isEmpty()) {
					terms.add(term);
				}
			}
			return ItemDAO.retrieve(terms);
		} else {
			/* if flag is null, then it is being searched by a specific identifier */
			/* parse the catID to int and pass it to the DAO */
			int catid = -1;
			try {
				catid = Integer.parseInt(searchTerm);
			} catch (Exception e) {
				throw new Exception("internal problem catID is not parsed to integer");
			}

			return ItemDAO.retrieve(catid);
		}

	}

	public void createOrders(String name, String userName, TreeMap<String, ItemBought> allproducts)
			throws DatatypeConfigurationException, JAXBException, IOException {
		// Using an object factory, we're creating an object that'll be parsed by the
		// marshaller to make an xml file
		// the root xml element is order
		// in it we have a customer with name and account (name and username)
		// then an itemlist that contains all the items
		// each item has a name, ID, price, and quantity, extended is price * quantity
		// then the order has shipping cost, an order number, hst, a grand total and the
		// date submitted
		// right now it's just using an absolute path because it's being wacky with
		// relative paths
        File Path = new File(poPath);
        if (!Path.exists()) {
            try{
            	Path.getParentFile().mkdirs();
            	Path.mkdirs();
            }
            catch(Exception e){
                //handle it
            	throw e;
            }           
        }
		CustomerType customer = factory.createCustomerType();
		ItemType item1 = factory.createItemType();
		ItemsType items = factory.createItemsType();
		OrderType order = factory.createOrderType();
		List<ItemType> itemsList = items.getItem();
		double total = 0.00;
		customer.setName(name);
		customer.setAccount(userName);
		order.setTotal(BigDecimal.ZERO);
		for (Map.Entry<String, ItemBought> entry : allproducts.entrySet()) {
			ItemBought bought = entry.getValue();
			ItemBean it = bought.getItem();
			ItemType item = factory.createItemType();
			item.setName(it.getName());
			item.setNumber(it.getNumber());
			item.setPrice(new BigDecimal(it.getPrice()).setScale(2, BigDecimal.ROUND_HALF_UP));
			item.setQuantity(BigInteger.valueOf(new Integer(bought.getQuantity()).intValue()));
			item.setExtended(item.getPrice().multiply(new BigDecimal(item.getQuantity())).setScale(2, BigDecimal.ROUND_HALF_UP));
			total+=item.getExtended().doubleValue();
			itemsList.add(item);
		}

		order.setTotal(new BigDecimal(total).setScale(2, BigDecimal.ROUND_HALF_UP));
		order.setCustomer(customer);
		order.setItems(items);
		order.setShipping(new BigDecimal(12).setScale(2, BigDecimal.ROUND_HALF_UP));
		order.setHST(order.getTotal().multiply(new BigDecimal(HST)).setScale(2, BigDecimal.ROUND_HALF_UP));
		order.setGrandTotal(order.getTotal().add(order.getHST().add(order.getShipping())).setScale(2, BigDecimal.ROUND_HALF_UP));
		GregorianCalendar c = new GregorianCalendar();
		XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		order.setSubmitted(date2);
		
		final File folder = new File(poPath);
		int number = 0;
		String[] fname = new String[3];
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.getName().contains(name))
			{
			fname = fileEntry.getName().split("\\.");
			fname = fname[0].split("_");
			}
		}
		if (fname[1] !=null)
		number = Integer.parseInt(fname[1]);
		number += 1;
		order.setId(BigInteger.valueOf(number));

		
		JAXBContext jc = JAXBContext.newInstance(order.getClass());
		Marshaller marsh = jc.createMarshaller();
		marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marsh.marshal(order,
				new File(poPath+ customer.getName() + "_" + new DecimalFormat("00").format(number) + ".xml"));


	}

	public List<CategoryBean> doCategory(String prefix) throws Exception {
		return CategoryDAO.retrieve(prefix);
	}

	public ItemBought createItem(String productId, String productName, String unitPrice, String _quantity,
			String unit) {

		ItemBean itemBean = null;
		ItemBought itemBought = new ItemBought();
		try {
			double price = Double.parseDouble(unitPrice);
			int quantity = Integer.parseInt(_quantity);

			itemBean = new ItemBean(0, productId, productName, price, unit);
			itemBought.setItem(itemBean);
			itemBought.setQuantity(quantity);
		} catch (Exception e) {
			throw e;
		}

		return itemBought;
	}

	public List<String> orderList(String name) {
		List<String> ordersList = new ArrayList<String>();
		final File folder = new File(poPath);

		String[] fname = new String[3];
		for (final File fileEntry : folder.listFiles()) {
			fname = fileEntry.getName().split("\\.");
			if (fname[0].contains(name))
			ordersList.add(fname[0]);
		}
		
		return ordersList;
	}

	public OrderType generateOrder(String order) {

		OrderType ord = new OrderType();
		try {
			JAXBContext jc = JAXBContext.newInstance(ord.getClass());
			Unmarshaller um = jc.createUnmarshaller();
			ord = (OrderType) um.unmarshal(new File(poPath + order + ".xml"));
		} catch (Exception e) {

			System.out.println(e.getMessage());

		}
		return ord;
	}

}