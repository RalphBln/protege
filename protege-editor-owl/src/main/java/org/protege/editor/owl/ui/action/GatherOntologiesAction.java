package org.protege.editor.owl.ui.action;

import org.protege.editor.owl.model.io.OntologySaver;
import org.protege.editor.owl.ui.GatherOntologiesPanel;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 22-May-2007<br><br>
 */
public class GatherOntologiesAction extends ProtegeOWLAction {

    public void actionPerformed(ActionEvent e) {
        // Need to pop a dialog asking where to save
        GatherOntologiesPanel panel = GatherOntologiesPanel.showDialog(getOWLEditorKit());
        if (panel == null) {
            return;
        }
        boolean errors = false;
        OWLDocumentFormat saveAsFormat = panel.getOntologyFormat();
        File saveAsLocation = panel.getSaveLocation();
        OntologySaver.Builder ontologySaverBuilder = OntologySaver.builder();
        for (OWLOntology ont : panel.getOntologiesToSave()) {
            final OWLDocumentFormat format;
            OWLOntologyManager man = getOWLModelManager().getOWLOntologyManager();
            if(saveAsFormat != null) {
                format = saveAsFormat;
            }
            else {
                OWLDocumentFormat documentFormat = man.getOntologyFormat(ont);
                if(documentFormat != null) {
                    format = documentFormat;
                }
                else {
                    format = new RDFXMLDocumentFormat();
                }
            }

            URI originalPhysicalURI = man.getOntologyDocumentIRI(ont).toURI();
            String originalPath = originalPhysicalURI.getPath();
            if (originalPath == null) {
                originalPath = UUID.randomUUID().toString() + ".owl";
            }
            File originalFile = new File(originalPath);
            String originalFileName = originalFile.getName();
            if (ont.getOntologyID().getVersionIRI().isPresent()) {
                // ontology has version IRI. File name might be just a version number, which is likely to lead to name clashes
                String lastVersionIRISegment = Arrays.stream(ont.getOntologyID().getVersionIRI().get().toString().split("/")).reduce((first, second) -> second).get();
                String lastOntologyIRISegment = Arrays.stream(ont.getOntologyID().getOntologyIRI().get().toString().split("/")).reduce((first, second) -> second).get();
                if (!lastVersionIRISegment.equals(lastOntologyIRISegment)) {
                    originalFileName = lastOntologyIRISegment + "." + lastVersionIRISegment;
                }
            }
            originalFileName += ".owl";
            File saveAsFile = new File(saveAsLocation, originalFileName);

            ontologySaverBuilder.addOntology(ont, format, IRI.create(saveAsFile));
        }
        try {
            ontologySaverBuilder.build().saveOntologies();
        }
        catch (OWLOntologyStorageException e1) {
            LoggerFactory.getLogger(GatherOntologiesAction.class)
                    .error("An error occurred whilst saving a gathered ontology: {}", e1);
            errors = true;
        }

        if (errors) {
            JOptionPane.showMessageDialog(getWorkspace(),
                                          "There were errors when saving the ontologies.  Please check the log for details.",
                                          "Error during save",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }


    public void initialise() throws Exception {
    }


    public void dispose() throws Exception {
    }
}
