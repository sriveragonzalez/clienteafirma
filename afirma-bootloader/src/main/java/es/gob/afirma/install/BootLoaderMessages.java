/*******************************************************************************
 * Este fichero forma parte del Cliente @firma.
 * El Cliente @firma es un aplicativo de libre distribucion cuyo codigo fuente puede ser consultado
 * y descargado desde http://forja-ctt.administracionelectronica.gob.es/
 * Copyright 2009,2010,2011 Gobierno de Espana
 * Este fichero se distribuye bajo  bajo licencia GPL version 2  segun las
 * condiciones que figuran en el fichero 'licence' que se acompana. Si se distribuyera este
 * fichero individualmente, deben incluirse aqui las condiciones expresadas alli.
 ******************************************************************************/

package es.gob.afirma.install;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/** Gestor de mensajes del BootLoader. */
final class BootLoaderMessages {

    private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault(), AOBootUtil.getCleanClassLoader());

    private BootLoaderMessages() {
        // No permitimos la instanciacion
    }

    /** Obtiene un mensaje de usuario.
     * @param key Clave del mensaje a obtener
     * @return Mensaje obtenido */
    static String getString(final String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        }
        catch (final MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
