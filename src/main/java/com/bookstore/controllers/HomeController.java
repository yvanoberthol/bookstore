package com.bookstore.controllers;

import com.bookstore.entities.content.*;
import com.bookstore.entities.security.PasswordResetToken;
import com.bookstore.entities.security.Role;
import com.bookstore.entities.security.User;
import com.bookstore.entities.security.UserRole;
import com.bookstore.services.content.BookService;
import com.bookstore.services.content.CartItemService;
import com.bookstore.services.content.OrderService;
import com.bookstore.services.content.UserPaymentService;
import com.bookstore.services.mail.MailConfig;
import com.bookstore.services.security.UserSecurityService;
import com.bookstore.services.security.UserService;
import com.bookstore.utility.SecurityUtility;
import com.bookstore.utility.USConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.security.Principal;
import java.util.*;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserPaymentService userPaymentService;

    @Autowired
    private UserSecurityService userSecurityService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartItemService cartItemService;


    @Autowired
    @Qualifier("sendMailer")
    private MailConfig mailConfig;





    /******************************/
    /* All Actions of PAGE INDEX or HOME */
    /******************************/

    @RequestMapping(value = {"/home","/"})
    public String home(){
        return "home";
    }



    /******************************/
    /* All Actions of USER ACCOUNT */
    /******************************/

    @RequestMapping(value = "/myAccount")
    public String myAccont(){
        return "myAccount";
    }


    /*Login page ou page de connexion*/
    @RequestMapping(value = "/login")
    public String login(Model model){
        model.addAttribute("classActiveLogin",true);
        return "myAccount";
    }

    /*our hours*/
    @RequestMapping(value = "/hours")
    public String hours(Model model){
        return "hours";
    }
    /*our faq*/
    @RequestMapping(value = "/faq")
    public String faq(Model model){
        return "faq";
    }

    /*forgot password or mot de passe oublié*/
    @RequestMapping(value = "/forgetPassword", method = RequestMethod.POST)
    public String forgetPassword(Model model,
                         HttpServletRequest request,
                         @ModelAttribute("email") String userEmail) throws Exception {

        model.addAttribute("classActiveForgot",true);
        User user = userService.findByEmail(userEmail);
        if ( user == null){
            model.addAttribute("emailNotExists",true);
            return "myAccount";
        }

        String password = SecurityUtility.randomPassword();
        System.out.println("The password is "+password);

        String encryptedPassword = SecurityUtility.passwordEncoder().encode(password);
        user.setPassword(encryptedPassword);


        userService.saveUser(user);

        PasswordResetToken passwordResetToken = userService.findTokenEmail(userEmail);
        String token = UUID.randomUUID().toString();
        passwordResetToken.setToken(token);

        userService.saveToken(passwordResetToken);

        String appUrl = "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();

        mailConfig.constructorResetTokenEmail(appUrl,request.getLocale(),token,user,password);

        model.addAttribute("forgetPasswordEmailSent",true);
        return "myAccount";
    }

    /*new user or creation d'un nouvel utilisateur*/
    @RequestMapping(value = "/newUser", method = RequestMethod.POST)
    public String addUser(HttpServletRequest request,
                          @ModelAttribute("email") String userEmail,
                          @ModelAttribute("username") String username,
                          Model model) throws Exception{
        model.addAttribute("classActiveNewAccount",true);
        model.addAttribute("email",userEmail);
        model.addAttribute("username",username);

        if (userService.findByUsername(username) !=null){
            model.addAttribute("usernameExists",true);
            return "myAccount";
        }

        if (userService.findByEmail(userEmail) !=null){
            model.addAttribute("emailExists",true);
            return "myAccount";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(userEmail);
        String password = SecurityUtility.randomPassword();
        System.out.println("The password is "+password);

        String encryptedPassword = SecurityUtility.passwordEncoder().encode(password);
        user.setPassword(encryptedPassword);

        Role role = new Role();
        role.setName("ROLE_USER");

        Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(new UserRole(user,role));
        userService.createUser(user,userRoles);

        String token = UUID.randomUUID().toString();
        userService.createPasswordResetTokenForUser(user,token);

        String appUrl = "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();

        mailConfig.constructorResetTokenEmail(appUrl,request.getLocale(),token,user,password);

        model.addAttribute("emailSent",true);
        return "myAccount";
    }


    /*new account or creation d'un nouveau compte*/
    @RequestMapping(value = "/newAccount")
    public String newAccount(Model model,
                             @RequestParam("token") String token){

        System.out.println("The token is: "+token);
        PasswordResetToken passwordResetToken = userService.getPasswordResetToken(token);
        System.out.println("The passwordResetToken is "+passwordResetToken);
        if (passwordResetToken == null){
            String message = "Invalid Token";
            model.addAttribute("message",message);
            return "redirect:/badRequest";
        }

        User user = passwordResetToken.getUser();
        String username = user.getUsername();
        System.out.println("the username is "+username);

        UserDetails userDetails = userSecurityService.loadUserByUsername(username);
        System.out.println("the userDetails is "+userDetails);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,userDetails.getPassword(),userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        model.addAttribute("user",user);
        model.addAttribute("classActiveEdit",true);
        return "myProfile";
    }

    // my profile ou modification et consultation de votre profil
    @RequestMapping(value = "/myProfile")
    public String myProfile(Model model,Principal principal){

        User user= userService.findByUsername(principal.getName());
        model.addAttribute("user",user);
        model.addAttribute("userPaymentList",user.getUserPaymentList());
        model.addAttribute("userShippingList",user.getUserShippingList());
        model.addAttribute("orderList",user.getOrderList());

        UserShipping userShipping = new UserShipping();
        model.addAttribute("userShipping",userShipping);

        model.addAttribute("listOfCreditCards",true);
        model.addAttribute("listOfShippingAddresses",true);

        List<String> stateList = USConstants.listOfUSStatesCode;
        Collections.sort(stateList);
        model.addAttribute("stateList",stateList);
        model.addAttribute("classActiveEdit",true);
        return "myProfile";
    }

    //update usr info or modification des infos de l'utilisateur
    @RequestMapping(value = "/updateUserInfo", method = RequestMethod.POST)
    public String updateUserInfo(@ModelAttribute("user") User user,
                                 @ModelAttribute("newPassword") String newPassword,
                                  Model model) throws Exception {
        User currentUser = userService.getUser(user.getId());
        if (currentUser == null){
            throw new Exception("user not found");
        }

        /*check email already exists*/
        if (userService.findByEmail(user.getEmail())!=null){
            if (!userService.findByEmail(user.getEmail()).getId().equals(currentUser.getId())){
                model.addAttribute("emailExists",true);
                model.addAttribute("classActiveEdit",true);
                return "myProfile";
            }
        }

        /*check username already exists*/
        if (userService.findByUsername(user.getUsername())!=null){
            if (!userService.findByUsername(user.getUsername()).getId().equals(currentUser.getId())){
                model.addAttribute("usernameExists",true);
                model.addAttribute("classActiveEdit",true);
                return "myProfile";
            }
        }

        if (newPassword != null && !newPassword.isEmpty()){
            BCryptPasswordEncoder passwordEncoder = SecurityUtility.passwordEncoder();
            String dbPassword = currentUser.getPassword();
            if (passwordEncoder.matches(user.getPassword(),dbPassword)){
                currentUser.setPassword(passwordEncoder.encode(newPassword));
            }else{
                model.addAttribute("incorrectPassword",true);
                model.addAttribute("classActiveEdit",true);
                return "myProfile";
            }
        }

        currentUser.setFirstName(user.getFirstName());
        currentUser.setLastName(user.getLastName());
        currentUser.setUsername(user.getUsername());
        currentUser.setEmail(user.getEmail());
        currentUser.setPhone(user.getPhone());

        userService.saveUser(currentUser);

        model.addAttribute("updateUserInfo",true);
        model.addAttribute("user",currentUser);
        model.addAttribute("classActiveEdit",true);

        model.addAttribute("listOfShippingAddresses",true);
        model.addAttribute("listOfCreditCards",true);

        UserDetails userDetails = userSecurityService.loadUserByUsername(currentUser.getUsername());
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,userDetails.getPassword(),userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return "myProfile";
    }

    /******************************/
    /* All Actions of BOOKS */
    /******************************/

    @RequestMapping(value = "/bookshelf")
    public String bookShelf(Model model, Principal principal){

        if (principal !=null){
            String username = principal.getName();
            User user = userService.findByUsername(username);
            model.addAttribute("user",user);
        }

        List<Book> bookList = bookService.findAll();
        model.addAttribute("activeAll",true);
        model.addAttribute("bookList",bookList);
        return "bookShelf";
    }

    @RequestMapping(value = "/bookDetail")
    public String bookDetail(Model model, @PathParam("id") Long id, Principal principal){

        if (principal !=null){
            String username = principal.getName();
            User user = userService.findByUsername(username);
            model.addAttribute("user",user);
        }

        Book book = bookService.getBook(id);
        model.addAttribute("book",book);

        List<Integer> qtyList = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
        model.addAttribute("qtyList",qtyList);
        model.addAttribute("qty",1);

        return "bookDetail";
    }





    /******************************/
    /* All Actions of USER PAYMENT */
    /******************************/

    //mettre a jour les infos de sa carte de credit
    @RequestMapping(value = "/updateCreditCard")
    public String updateCreditCard(@ModelAttribute("id") Long creditCardId,Model model,Principal principal){
        User user= userService.findByUsername(principal.getName());
        UserPayment userPayment = userPaymentService.getUserPayment(creditCardId);

        if (!user.getId().equals(userPayment.getUser().getId())){
            return "badRequestPage";
        }else {
            model.addAttribute("user",user);
            UserBilling userBilling = userPayment.getUserBilling();
            model.addAttribute("userPayment",userPayment);
            model.addAttribute("userBilling",userBilling);

            List<String> stateList = USConstants.listOfUSStatesCode;
            Collections.sort(stateList);

            List<String> countryList = USConstants.listOfCountryCode;
            Collections.sort(countryList);
            model.addAttribute("stateList",stateList);
            model.addAttribute("countryList",countryList);
            model.addAttribute("userPaymentList",user.getUserPaymentList());
            model.addAttribute("userShippingList",user.getUserShippingList());
            model.addAttribute("orderList",user.getOrderList());

            model.addAttribute("addNewCreditCard",true);
            model.addAttribute("classActiveBilling",true);
            model.addAttribute("listOfShippingAddresses",true);

        }

        return "myProfile";
    }

    //supprimer une carte de credit enregistrée
    @RequestMapping(value = "/removeCreditCard")
    public String removeCreditCard(@ModelAttribute("id") Long creditCardId,Model model,Principal principal){
        User user= userService.findByUsername(principal.getName());
        UserPayment userPayment = userPaymentService.getUserPayment(creditCardId);

        if (!user.getId().equals(userPayment.getUser().getId())){
            return "badRequestPage";
        }else {
            model.addAttribute("user",user);
            userPaymentService.removeUserPayment(creditCardId);

            model.addAttribute("userPaymentList",user.getUserPaymentList());
            model.addAttribute("userShippingList",user.getUserShippingList());
            model.addAttribute("orderList",user.getOrderList());

            model.addAttribute("listOfCreditCards",true);
            model.addAttribute("classActiveBilling",true);
            model.addAttribute("listOfShippingAddresses",true);

        }

        return "myProfile";
    }

    //definir la carte de credit de paoement par defaut
    @RequestMapping(value = "/setDefaultPayment", method = RequestMethod.POST)
    public String setDefaultPayment(@ModelAttribute("userPaymentId") Long userPaymentId,Model model,Principal principal){
        User user= userService.findByUsername(principal.getName());
        userPaymentService.setUserDefaultPayment(userPaymentId,user);
        model.addAttribute("user",user);
        model.addAttribute("userPaymentList",user.getUserPaymentList());
        model.addAttribute("userShippingList",user.getUserShippingList());
        model.addAttribute("orderList",user.getOrderList());

        model.addAttribute("listOfCreditCards",true);
        model.addAttribute("classActiveBilling",true);
        model.addAttribute("listOfShippingAddresses",true);

        return "myProfile";

    }

    // acceder au formulaire d'ajout d'une nouvelle carte de credit
    @RequestMapping("/addNewCreditCard")
    public String formAddNewCreditCard(Model model,Principal principal){
        User user= userService.findByUsername(principal.getName());
        model.addAttribute("user",user);
        model.addAttribute("userPaymentList",user.getUserPaymentList());
        model.addAttribute("userShippingList",user.getUserShippingList());
        model.addAttribute("orderList",user.getOrderList());

        model.addAttribute("userBilling",new UserBilling());
        model.addAttribute("userPayment",new UserPayment());

        List<String> stateList = USConstants.listOfUSStatesCode;
        Collections.sort(stateList);

        List<String> countryList = USConstants.listOfCountryCode;
        Collections.sort(countryList);
        model.addAttribute("stateList",stateList);
        model.addAttribute("countryList",countryList);
        model.addAttribute("addNewCreditCard",true);
        model.addAttribute("classActiveBilling",true);
        model.addAttribute("listOfShippingAddresses",true);

        return "myProfile";
    }

    //ajouter une nouvelle carte de credit
    @RequestMapping(value = "/addNewCreditCard", method = RequestMethod.POST)
    public String addNewCreditCard(@ModelAttribute("userPayment") UserPayment userPayment,
                                   @ModelAttribute("userBilling") UserBilling userBilling,
                                   Principal principal, Model model){
        User user= userService.findByUsername(principal.getName());
        if (user != null){
            System.out.println("user : "+user);
            if (userPayment.getIdPayment() != null) {
                System.out.println("user MAJ");
                userPaymentService.addUserBilling(userBilling, userPayment,user);
            }else{
                System.out.println("user add");
                userPaymentService.addUserBilling(userBilling, userPayment, user);
            }
            model.addAttribute("user", user);
            model.addAttribute("userPaymentList", user.getUserPaymentList());
            model.addAttribute("userShippingList", user.getUserShippingList());
            model.addAttribute("orderList",user.getOrderList());
            model.addAttribute("listOfCreditCards", true);
            model.addAttribute("classActiveBilling", true);
            model.addAttribute("listOfShippingAddresses", true);
        }else {
            System.out.println("user : "+ null);
            return "badRequestPage";
        }

        return "myProfile";
    }

    //liste de toutes les cartes de credit
    @RequestMapping("/listOfCreditCards")
    public String listOfCreditCards(Model model,Principal principal){
        User user= userService.findByUsername(principal.getName());
        model.addAttribute("user",user);
        model.addAttribute("userPaymentList",user.getUserPaymentList());
        model.addAttribute("userShippingList",user.getUserShippingList());
        model.addAttribute("orderList",user.getOrderList());
        model.addAttribute("no",true);
        model.addAttribute("listOfCreditCards",true);
        model.addAttribute("classActiveBilling",true);
        model.addAttribute("listOfShippingAddresses",true);

        return "myProfile";
    }


    /******************************/
    /* All Actions of USER SHIPPING */
    /******************************/

    //add new shipping address or formulaire d'adresse d'expedition
    @RequestMapping("/addNewShippingAddress")
    public String addNewShippingAddress(Model model,Principal principal){
        User user= userService.findByUsername(principal.getName());
        model.addAttribute("user",user);
        model.addAttribute("userPaymentList",user.getUserPaymentList());
        model.addAttribute("userShippingList",user.getUserShippingList());
        model.addAttribute("orderList",user.getOrderList());

        model.addAttribute("userShipping",new UserShipping());

        List<String> stateList = USConstants.listOfUSStatesCode;
        Collections.sort(stateList);

        List<String> countryList = USConstants.listOfCountryCode;
        Collections.sort(countryList);
        model.addAttribute("countryList",countryList);
        model.addAttribute("stateList",stateList);
        model.addAttribute("addNewShippingAddress",true);
        model.addAttribute("classActiveShipping",true);
        model.addAttribute("listOfCreditCards",true);

        return "myProfile";
    }

    //add new shipping address or ajouter des adresses d'expeditions
    @RequestMapping(value = "/addNewShippingAddress", method = RequestMethod.POST)
    public String addNewShippingAddress(@ModelAttribute("userShipping") UserShipping userShipping,
                                        Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName());
        if (user != null) {
            System.out.println("user : " + user);
            if (userShipping.getIdShipping() != null) {
                userPaymentService.addUserShipping(userShipping, user);
            } else {
                userPaymentService.addUserShipping(userShipping, user);
                System.out.println("usershipping add");
            }
            model.addAttribute("user", user);
            model.addAttribute("userPaymentList", user.getUserPaymentList());
            model.addAttribute("userShippingList", user.getUserShippingList());
            model.addAttribute("orderList",user.getOrderList());
            model.addAttribute("classActiveShipping", true);
            model.addAttribute("listOfShippingAddresses", true);
            model.addAttribute("listOfCreditCards",true);
        } else {
            System.out.println("user : " + null);
            return "badRequestPage";
        }

        return "myProfile";
    }

    //update shipping address or modifier les adresses d'expeditions
    @RequestMapping(value = "/updateShippingAddress")
    public String updateShippingAddress(@ModelAttribute("id") Long shippingId,Model model,Principal principal){
        User user= userService.findByUsername(principal.getName());
        UserShipping userShipping = userPaymentService.getUserShipping(shippingId);

        if (!user.getId().equals(userShipping.getUser().getId())){
            return "badRequestPage";
        }else {
            model.addAttribute("user",user);
            model.addAttribute("userShipping",userShipping);

            List<String> stateList = USConstants.listOfUSStatesCode;
            Collections.sort(stateList);

            List<String> countryList = USConstants.listOfCountryCode;
            Collections.sort(countryList);
            model.addAttribute("stateList",stateList);
            model.addAttribute("countryList",countryList);
            model.addAttribute("userPaymentList",user.getUserPaymentList());
            model.addAttribute("userShippingList",user.getUserShippingList());
            model.addAttribute("orderList",user.getOrderList());

            model.addAttribute("addNewShippingAddress",true);
            model.addAttribute("classActiveShipping",true);
            model.addAttribute("listOfCreditCards",true);

        }

        return "myProfile";
    }

    //supprimer une adresse d'expedition
    @RequestMapping(value = "/removeShippingAddress")
    public String removeShippingAddress(@ModelAttribute("id") Long shippingId,Model model,Principal principal){
        User user= userService.findByUsername(principal.getName());
        UserShipping userShipping = userPaymentService.getUserShipping(shippingId);

        if (!user.getId().equals(userShipping.getUser().getId())){
            return "badRequestPage";
        }else {
            model.addAttribute("user",user);
            userPaymentService.removeUserShipping(shippingId);

            model.addAttribute("userPaymentList",user.getUserPaymentList());
            model.addAttribute("userShippingList",user.getUserShippingList());
            model.addAttribute("orderList",user.getOrderList());

            model.addAttribute("listOfShippingAddresses",true);
            model.addAttribute("classActiveShipping",true);
            model.addAttribute("listOfCreditCards",true);

        }

        return "myProfile";
    }

    //definir l'adresse d'expedition par defaut
    @RequestMapping(value = "/setDefaultShippingAddress", method = RequestMethod.POST)
    public String setDefaultShippingAddress(@ModelAttribute("userShippingId") Long userShippingId,Model model,Principal principal){
        User user= userService.findByUsername(principal.getName());
        userPaymentService.setUserDefaultShipping(userShippingId,user);
        model.addAttribute("user",user);
        model.addAttribute("userPaymentList",user.getUserPaymentList());
        model.addAttribute("userShippingList",user.getUserShippingList());
        model.addAttribute("orderList",user.getOrderList());

        model.addAttribute("listOfShippingAddresses",true);
        model.addAttribute("classActiveShipping",true);
        model.addAttribute("listOfCreditCards",true);
        return "myProfile";

    }

    //liste de tous vos adresses d'expeditions
    @RequestMapping("/listOfShippingAddresses")
    public String listOfShippingAddresses(Model model,Principal principal){
        User user= userService.findByUsername(principal.getName());
        model.addAttribute("user",user);
        model.addAttribute("userPaymentList",user.getUserPaymentList());
        model.addAttribute("userShippingList",user.getUserShippingList());
        model.addAttribute("orderList",user.getOrderList());

        model.addAttribute("classActiveShipping",true);
        model.addAttribute("listOfShippingAddresses",true);
        model.addAttribute("listOfCreditCards",true);

        return "myProfile";
    }


    /******************************/
    /* All Actions of Order */
    /******************************/

    @RequestMapping(value = "/orderDetail")
    public String orderDetail(@RequestParam("id") Long orderId,
                              Model model, Principal principal){
        User user= userService.findByUsername(principal.getName());
        Order order = orderService.getOrder(orderId);

        if (!order.getUser().getId().equals(user.getId())){
            return "badRequestPage";
        }else{

            List<CartItem> cartItemList = cartItemService.findByOrder(order);
            model.addAttribute("user",user);
            model.addAttribute("order",order);
            model.addAttribute("cartItemList",cartItemList);

            model.addAttribute("userPaymentList",user.getUserPaymentList());
            model.addAttribute("userShippingList",user.getUserShippingList());
            model.addAttribute("orderList",user.getOrderList());

            List<String> stateList = USConstants.listOfUSStatesCode;
            Collections.sort(stateList);

            List<String> countryList = USConstants.listOfCountryCode;
            Collections.sort(countryList);
            model.addAttribute("stateList",stateList);
            model.addAttribute("countryList",countryList);
            //model.addAttribute("orderList",user.getOrderList());

            model.addAttribute("listOfShippingAddresses",true);
            model.addAttribute("listOfCreditCards",true);

            //order activation
            model.addAttribute("classActiveOrder",true);
            model.addAttribute("displayOrderDetail",true);

            return "myProfile";
        }

    }
}
