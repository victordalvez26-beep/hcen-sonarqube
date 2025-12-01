package uy.edu.tse.hcen.service;

import jakarta.ejb.Stateless;

@Stateless
public class NodoIntegrationServiceBean implements NodoIntegrationService {

    // Integration checks removed to simplify to CRUD-only flow. This
    // implementation intentionally performs no external checks. It exists
    // to satisfy injections elsewhere until consumer code is fully removed.
    @Override
    public void checkAndUpdateEstado(uy.edu.tse.hcen.model.NodoPeriferico nodo) {
        // no-op
    }
}
