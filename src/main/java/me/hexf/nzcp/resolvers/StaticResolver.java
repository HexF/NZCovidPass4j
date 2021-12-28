package me.hexf.nzcp.resolvers;

import foundation.identity.did.DIDDocument;
import me.hexf.nzcp.exceptions.DocumentNotFoundException;
import me.hexf.nzcp.exceptions.DocumentResolvingException;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StaticResolver implements IResolver{
    private final Map<URI, DIDDocument> resolvers;

    protected StaticResolver(Map<URI, DIDDocument> resolvers){
        this.resolvers = resolvers;
    }

    public static class Builder {
        private final HashMap<URI, DIDDocument> resolvers = new HashMap<>();

        public Builder addDocument(URI uri, DIDDocument document){
            resolvers.put(uri, document);
            return this;
        }

        public StaticResolver build(){
            return new StaticResolver(Collections.unmodifiableMap(this.resolvers));
        }
    }

    @Override
    public DIDDocument resolveDidDocument(URI didLocator) throws DocumentResolvingException {
        if(!resolvers.containsKey(didLocator))
            throw new DocumentNotFoundException(didLocator);

        return resolvers.get(didLocator);
    }
}
