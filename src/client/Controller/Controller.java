package client.Controller;

import client.Model.ClientConnection;
import common.Exceptions.*;
import common.Exceptions.UnknownError;
import common.Flight;
import common.Pair;
import common.StopOvers;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

public class Controller {

    private ClientConnection connection;

    public Controller(ClientConnection connection){
        this.connection=connection;
    }

    public String[] login(String username,char[]password) throws IOException, WrongFrameTypeException, InterruptedException {
        return connection.login(username,password);
    }

    public void register(String username, char[] password) throws IOException, WrongFrameTypeException, FlightNotFoundException, DayClosedException, AccountException, WrongCredentials, MaxFlightsException, UnknownError, BookingNotFound, IncompatibleFlightsException, FlightFullException, InterruptedException {
       connection.registarCliente(username,password);
    }

    public void cancelDay(LocalDate date) throws DateTimeException, FlightNotFoundException, DayClosedException, AccountException, WrongCredentials, UnknownError, BookingNotFound, IOException, FlightFullException, MaxFlightsException, IncompatibleFlightsException, InterruptedException {
       if(LocalDate.now().isAfter(date)){
           throw new DateTimeException("");
       }
        connection.cancelaDia(date);
    }

    public String adicionaDefaultFlight(String origem, String destino, String capacidade) throws IOException, FlightNotFoundException, DayClosedException, AccountException, WrongCredentials, UnknownError, BookingNotFound, FlightFullException, MaxFlightsException, IncompatibleFlightsException, InterruptedException {
        return connection.adicionaDefaultFlight(origem,destino,capacidade);
    }

    public String[] getCityNames() throws IOException, InterruptedException {
        return connection.getCities().toArray(new String[0]);
    }

    public List<Pair<LocalDate,StopOvers>> getPossibleBookings(String orig, String dest, LocalDate dateBegin, LocalDate dateEnd) throws IOException, InterruptedException {
        return connection.getPossibleBookings(orig, dest, dateBegin, dateEnd);
    }

    public String specificReservation(List<String> stopOvers,LocalDate dateBegin,LocalDate dateEnd) throws IOException, BookingNotFound, FlightNotFoundException, WrongCredentials, DayClosedException, WrongFrameTypeException, FlightFullException, AccountException, UnknownError, MaxFlightsException, IncompatibleFlightsException, InterruptedException {
        if(dateBegin.isAfter(dateEnd) || LocalDate.now().isAfter(dateBegin)){
            throw new DateTimeException("");
        }
        return connection.specificReservation(stopOvers, dateBegin, dateEnd);
    }

    public String reservation(StopOvers stopOvers,LocalDate date) throws IOException, BookingNotFound, FlightNotFoundException, WrongCredentials, FlightFullException, WrongFrameTypeException, DayClosedException, AccountException, UnknownError, MaxFlightsException, IncompatibleFlightsException, InterruptedException {
        return connection.reservation(stopOvers,date);
    }

    public String[] getBookings() throws IOException, InterruptedException {
        return connection.getBookingsFromAccount().toArray(new String[0]);
    }

    public List<Flight> getAllFlights() throws IOException, WrongFrameTypeException, InterruptedException {
        return connection.allFlights();
    }

    public void cancelBooking(String id) throws IOException, BookingNotFound, FlightNotFoundException, WrongCredentials, FlightFullException, IncompatibleFlightsException, DayClosedException, MaxFlightsException, AccountException, UnknownError, InterruptedException {
        connection.cancelaBooking(id);
    }

    public String getLoggedUser(){
        return connection.getLoggedUser();
    }
}














