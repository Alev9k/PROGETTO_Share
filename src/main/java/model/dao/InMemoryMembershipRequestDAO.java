package model.dao;

import model.entity.MembershipRequest;

import java.util.ArrayList;
import java.util.List;

public class InMemoryMembershipRequestDAO implements MembershipRequestDAO {
    private final List<MembershipRequest> requests = new ArrayList<>();

    @Override
    public void save(MembershipRequest request) {
        requests.add(request);
    }

    @Override
    public void update(MembershipRequest updatedRequest) {
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getRequestId().equals(updatedRequest.getRequestId())) {
                requests.set(i, updatedRequest);
                return;
            }
        }
        throw new IllegalArgumentException("Richiesta di accesso non trovata.");
    }

    @Override
    public List<MembershipRequest> findAll() {
        return new ArrayList<>(requests);
    }
}
