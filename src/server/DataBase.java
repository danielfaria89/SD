package server;

import common.Account;
import common.Exceptions.*;
import common.Flight;
import common.Booking;
import common.StopOvers;

import java.io.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataBase {

    private static final String FLIGHTS_FILE="src/server/Files/Flights";
    private static final String ACCOUNTS_FILE="src/server/Files/Accounts";
    private static final String BOOKING_FILE="src/server/Files/Bookings";

    private ColBookings bookings;
    private Map<String, Account> accounts;

    private ReadWriteLock l = new ReentrantReadWriteLock();
    private Lock l_r = l.readLock();
    private Lock l_w = l.writeLock();

    //Construtors
    public DataBase(){
        try{
            accounts=new HashMap<>();
            readAccounts(ACCOUNTS_FILE);
            bookings=new ColBookings(new FlightCalculator(FLIGHTS_FILE));
            readBookings(BOOKING_FILE);
        } catch (IOException | FlightNotFoundException | DayClosedException | MaxFlightsException | IncompatibleFlightsException | FlightFullException e) {
            e.printStackTrace();
        }
    }

    //FUNCIONALIDADE 1:DONE

    public void addClient(String username,char[]password, boolean admin) throws AccountException {
        l_w.lock();
        try {
            if(accounts.containsKey(username))throw new AccountException();
            else{
                accounts.put(username,new Account(username,password,admin));
            }
        }
        finally{
            l_w.unlock();
        }
    }

    public void addClient(Account c) throws AccountException {
        c.l.lock();
        l_w.lock();
        try {
            if(accounts.containsKey(c.getUsername()))throw new AccountException();
            else{
                accounts.put(c.getUsername(),new Account(c.getUsername(),c.getPassword(),c.isAdmin()));
            }
        }
        finally{
            c.l.unlock();
            l_w.unlock();
        }
    }

    public boolean checkLogIn(String id, char[] pass){
        l_r.lock();
        try{
            Account c = accounts.get(id);
            c.l.lock();
            try{
                return Arrays.equals(c.getPassword(), pass);
            }finally {
                c.l.unlock();

            }
        }
        finally{
            l_r.unlock();
        }
    }

    //FUNCIONALIDADE 2:DONE

    public boolean checkAdmin(String id) throws AccountException {
        l_r.lock();
        try{
            if(!accounts.containsKey(id)) throw new AccountException();
            else{
                Account c = accounts.get(id);
                c.l.lock();
                try {
                    return c.isAdmin();
                }finally {
                    c.l.unlock();
                }
            }
        }
        finally {
            l_r.unlock();
        }
    }

    //FUNCIONALIDADE 3:DONE

    public void addDefaultFlight(Flight flight) throws FlightException {
            bookings.addDefaultFlight(flight);
    }

    //FUNCIONALIDADE 4: DONE

    public void cancelDay(LocalDate date) throws DateTimeException{
        try{
            Set<String> cancelledBookings= bookings.cancelDay(date,l_w);
            for(String notif : cancelledBookings){
                String[] notificacao = notif.split(" ");
                accounts.get(notificacao[0]).adicionarNotificacao(notificacao[1]);
            }
        }finally {
            l_w.unlock();
        }
    }

    public void adicionarNotificacaoACliente(String id, String not){
        l_r.lock();
        try{
            Account c = accounts.get(id);
            c.l.lock();
            try {
                c.adicionarNotificacao(not);
            }finally {
                c.l.unlock();
            }
        }finally {
            l_r.unlock();
        }
    }

    public Set<String> getNotificacoesCliente(String id){
        l_r.lock();
        try {
            Account c = accounts.get(id);
            c.l.lock();
            try {
                return c.getNotifications(true);
            }finally {
                c.l.unlock();
            }
        }finally {
            l_r.unlock();
        }
    }

    //FUNCIONALIDADE 5 mix ADICIONAL 1:

    public Set<String> getAllCities(){
        return bookings.getAllCities();
    }

    public List<Booking> possibleBookings(String origin, String destination, LocalDate start, LocalDate end){
        if(start.isAfter(end))return null;
        List<LocalDate> dates = Stream.iterate(start, date -> date.plusDays(1))
                            .limit(ChronoUnit.DAYS.between(start, end)+1)
                            .collect(Collectors.toList());
        return bookings.getPossibleBookings(origin,destination,dates);
    }

    public void addBooking(Booking booking) throws FlightFullException, FlightNotFoundException, DayClosedException {
        bookings.addBooking(booking);
        accounts.get(booking.getClientID()).addBooking(booking.getBookingID());
    }

    public String registerBooking(String idCliente,List<String> percurso, LocalDate start, LocalDate end) throws DayClosedException, MaxFlightsException, FlightFullException, IncompatibleFlightsException, FlightNotFoundException { // Funcionalidade 5
        if(start.isAfter(end)) return null;
        List<LocalDate> dates = Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, end)+1)
                .collect(Collectors.toList());
        String res = bookings.getFirstBooking(idCliente,percurso,dates);
        if(res.length() == 7)
            accounts.get(idCliente).addBooking(res);
        return res;
    }

    //FUNCIONALIDADE 6:

    public void cancelBooking(String bookingID,String clientID) throws BookingNotFound, DayClosedException, AccountException {
        l_w.lock();
        try {
            bookings.cancelBooking(bookingID, clientID);
            this.accounts.get(clientID).removeBooking(bookingID);
        }finally {
            l_w.unlock();
        }
    }

    public List<Flight> getFlightsFromBooking(String id){
        return bookings.getFlightsFromBooking(id);
    }

    public Account getAccount(String id){
        l_r.lock();
        try {
            return accounts.get(id).clone();
        }finally {
            l_r.unlock();
        }
    }

    //FUNCIONALIDADE 7:


    public Set<Flight> getDefaultFlights() throws IOException {
        return bookings.getDefaultFlights();
    }

    //OTHERS

    public void clearOldFlights(){
        List<Booking> rem_book = bookings.clearOldFlights();
        for(Booking b : rem_book){
            this.accounts.get(b.getClientID()).removeBooking(b.getBookingID());
        }
    }

    private void readAccounts(String filename) throws IOException {
        BufferedReader reader = new BufferedReader((new FileReader(filename)));
        String line;
        while ((line = reader.readLine())!=null){
            String[] strings = line.split(";");
            boolean b = Boolean.parseBoolean(strings[2]);
            Account a = new Account(strings[0],strings[1].toCharArray(),b);
            for(int i = 3; i<strings.length; i++){
                a.addBooking(strings[i]);
            }
            accounts.put(a.getUsername(),a);
        }
    }

    public void writeAccounts(String filename) throws IOException {
        PrintWriter writer = new PrintWriter((new FileWriter(filename)));
        for(Account a : accounts.values()){
            StringBuilder strings = new StringBuilder();
            for(String s : a.getBookingsIds()){
                strings.append(";");
                strings.append(s);
            }
            StringBuilder strings2 = new StringBuilder();
            for(char c : a.getPassword()){
                strings2.append(c);
            }
            writer.println(a.getUsername() + ";" +
                    strings2 + ";" +
                    a.isAdmin() +
                    strings) ;
        }
        writer.flush();
        writer.close();
    }

    private void readBookings(String filename) throws IOException, FlightNotFoundException, DayClosedException, FlightFullException, MaxFlightsException, IncompatibleFlightsException {
        BufferedReader reader = new BufferedReader((new FileReader(filename)));
        String line;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while ((line = reader.readLine())!=null){
            String[] strings = line.split(";");
            List<Flight> flights = new ArrayList<>();
            for(int i = 3; i < strings.length; i++){
                flights.add(bookings.getDefaultFlight(strings[i]));
            }
            Booking a = new Booking(strings[0], strings[1],LocalDate.parse(strings[2], formatter),flights);
            addBooking(a);
        }
    }

    public void writeBookings(String filename) throws IOException {
        bookings.writeBookings(filename);
    }

    public void writeFlights(String filename) throws IOException {
        bookings.writeFlights(filename);
    }
}
