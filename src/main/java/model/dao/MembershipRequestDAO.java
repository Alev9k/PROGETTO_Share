package model.dao;

import model.entity.MembershipRequest;
import model.entity.MembershipRequestStatus;

import java.util.List;

public interface MembershipRequestDAO {
    void save(MembershipRequest request);
    void update(MembershipRequest request);
    List<MembershipRequest> findAll();

    default MembershipRequest findById(String requestId) {
        return findAll().stream()
                .filter(request -> request.getRequestId().equals(requestId))
                .findFirst()
                .orElse(null);
    }

    default List<MembershipRequest> findByGroupId(int groupId) {
        return findAll().stream()
                .filter(request -> request.getGroupId() == groupId)
                .toList();
    }

    default List<MembershipRequest> findByOperatorUsername(String username) {
        return findAll().stream()
                .filter(request -> request.getOperatorUsername().equals(username))
                .toList();
    }

    default MembershipRequest findPending(int groupId, String operatorUsername) {
        return findAll().stream()
                .filter(request -> request.getGroupId() == groupId)
                .filter(request -> request.getOperatorUsername().equals(operatorUsername))
                .filter(request -> request.getStatus() == MembershipRequestStatus.PENDING)
                .findFirst()
                .orElse(null);
    }

    default boolean hasAcceptedRequest(int groupId, String operatorUsername) {
        return findAll().stream()
                .anyMatch(request -> request.getGroupId() == groupId
                        && request.getOperatorUsername().equals(operatorUsername)
                        && request.getStatus() == MembershipRequestStatus.ACCEPTED);
    }
}
