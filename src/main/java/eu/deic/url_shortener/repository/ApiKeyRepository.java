package eu.deic.url_shortener.repository;

import eu.deic.url_shortener.domain.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByKeyHashAndActiveTrue(String keyHash);

    List<ApiKey> findByOwner(String owner);

    List<ApiKey> findByKeyPrefixAndActiveTrue(String keyPrefix);
}
