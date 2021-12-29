package me.hexf.nzcp.resolvers;

import foundation.identity.did.DIDDocument;
import me.hexf.nzcp.exceptions.DocumentResolvingException;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.HashMap;

public class CachingResolver implements IResolver {
    private final IResolver resolver;
    private final HashMap<URI, CacheEntry> cache = new HashMap<>();

    private final TemporalAmount timeToLive;

    private static class CacheEntry {
        private final DIDDocument document;
        private final LocalDateTime expiryTime;

        private CacheEntry(DIDDocument document, LocalDateTime expiryTime){
            this.document = document;
            this.expiryTime = expiryTime;
        }
    }

    public CachingResolver(IResolver resolver, TemporalAmount timeToLive){
        this.resolver = resolver;
        this.timeToLive = timeToLive;
    }

    @Override
    public DIDDocument resolveDidDocument(URI didLocator) throws DocumentResolvingException {

        CacheEntry entry = cache.get(didLocator);
        if(entry != null && entry.expiryTime.isAfter(LocalDateTime.now())){
            return entry.document;
        }


        DIDDocument resolvedDocument = resolver.resolveDidDocument(didLocator);
        entry = new CacheEntry(
                resolvedDocument,
                LocalDateTime.now().plus(timeToLive)
                );

        if(cache.containsKey(didLocator)) {
            cache.replace(didLocator, entry);
        } else {
            cache.put(didLocator, entry);
        }

        return resolvedDocument;

    }

}
