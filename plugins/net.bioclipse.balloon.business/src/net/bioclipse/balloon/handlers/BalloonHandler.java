/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.balloon.handlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.bioclipse.balloon.business.Activator;
import net.bioclipse.balloon.business.IBalloonManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class BalloonHandler extends AbstractHandler {
    
    private static final Logger logger = Logger.getLogger(BalloonHandler.class);

    /**
     * The constructor.
     */
    public BalloonHandler() {
    }

    /**
     * the command has been executed, so extract extract the needed information
     * from the application context.
     */
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

        ISelection sel = HandlerUtil.getCurrentSelection( event );
        if (sel==null) return null;
        if (!( sel instanceof StructuredSelection )) return null;
        IStructuredSelection ssel = (IStructuredSelection)sel;

        //We operate on files and IMolecules
        List<String> filenames=new ArrayList<String>();
        List<IResource> foldersToRefresh=new ArrayList<IResource>();
        //Collect files
        for (Object obj : ssel.toList()){
            if ( obj instanceof IFile ) {
                IFile file = (IFile) obj;
//                filenames.add( file.getRawLocation().toOSString() );
                filenames.add( file.getFullPath().toOSString() );
                foldersToRefresh.add( file.getParent() );
            }
        }
        
        logger.debug( "Balloon selection contained: " + filenames.size() + " files." );

        if (filenames.size()<=0) return null;
        
        //Run balloon on these files
        IBalloonManager balloon = Activator.getDefault().getBalloonManager();
        List<String> ret=null;
        try {
            ret = balloon.generate3Dcoordinates( filenames );
        } catch ( BioclipseException e ) {
            logger.error("Balloon failed: " + e);
        }

        if (ret==null){
            logger.error( "Balloon failed: " + ret );
            return null;
        }
        for (String r : ret){
            logger.debug( "Balloon wrote output file: " + r  );
        }
        
        //refresh folders
        for (IResource res : foldersToRefresh){
            try {
                res.refreshLocal( IResource.DEPTH_ONE, new NullProgressMonitor() );
            } catch ( CoreException e ) {
                logger.error( "Could not refresh resource: " + res + " - " + e.getMessage() );
            }
        }

        /*
        //Collect and serialize smiles to temp file, 
        String smilesfile="";
        String linesep=System.getProperty("line.separator");
        if (linesep==null) linesep="\n";
        
        for (Object obj : ssel.toList()){
            if ( obj instanceof IMolecule ) {
                IMolecule mol = (IMolecule) obj;
                String smilestext;
                try {
                    smilestext = mol.getSMILES();
                    smilesfile=smilesfile+smilestext+ linesep;
                } catch ( BioclipseException e ) {
                    logger.debug("Could not get smiles from Imol: " + mol 
                                 + ". Skipped in balloon: " + e.getMessage());
                }
            }
        }


        
        try {
            File tfile = File.createTempFile( "balloontemp", "smi" );
            FileWriter writer= new FileWriter(tfile);
            writer.write( smilesfile );
            writer.close();
            
            //Run balloon on this file
            
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */
        
        return null;

    }
}