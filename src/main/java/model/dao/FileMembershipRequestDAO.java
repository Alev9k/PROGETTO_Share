package model.dao;

import exceptions.DAOException;
import model.entity.MembershipRequest;
import model.entity.MembershipRequestStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Persistenza CSV dedicata alle richieste di accesso. */
public class FileMembershipRequestDAO implements MembershipRequestDAO {
    private static final String FILE_NAME = "membership_requests.csv";

    @Override
    public void save(MembershipRequest request) {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            writeRequest(out, request);
        } catch (IOException e) {
            throw new DAOException("Impossibile salvare la richiesta di accesso.");
        }
    }

    @Override
    public void update(MembershipRequest updatedRequest) {
        List<MembershipRequest> requests = findAll();
        boolean found = false;
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getRequestId().equals(updatedRequest.getRequestId())) {
                requests.set(i, updatedRequest);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new DAOException("Richiesta di accesso non trovata.");
        }
        rewriteFile(requests);
    }

    @Override
    public List<MembershipRequest> findAll() {
        List<MembershipRequest> requests = new ArrayList<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return requests;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length != 6) {
                    throw new IllegalArgumentException("Riga richiesta di accesso non valida.");
                }
                requests.add(new MembershipRequest(
                        parts[0],
                        Integer.parseInt(parts[1]),
                        parts[2],
                        MembershipRequestStatus.valueOf(parts[3]),
                        LocalDateTime.parse(parts[4]),
                        Boolean.parseBoolean(parts[5])
                ));
            }
            return requests;
        } catch (IOException | IllegalArgumentException e) {
            throw new DAOException("Impossibile leggere le richieste di accesso.");
        }
    }

    private void rewriteFile(List<MembershipRequest> requests) {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME, false))) {
            for (MembershipRequest request : requests) {
                writeRequest(out, request);
            }
        } catch (IOException e) {
            throw new DAOException("Impossibile aggiornare le richieste di accesso.");
        }
    }

    private void writeRequest(PrintWriter out, MembershipRequest request) {
        out.println(String.join(",",
                request.getRequestId(),
                Integer.toString(request.getGroupId()),
                request.getOperatorUsername(),
                request.getStatus().name(),
                request.getCreatedAt().toString(),
                Boolean.toString(request.isResultRead())
        ));
    }
}
