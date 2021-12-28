package me.hexf.nzcp.resolvers;

import foundation.identity.did.DIDDocument;
import me.hexf.nzcp.exceptions.DocumentResolvingException;

import java.net.URI;

public interface IResolver {
    DIDDocument resolveDidDocument(URI didLocator) throws DocumentResolvingException;
}
