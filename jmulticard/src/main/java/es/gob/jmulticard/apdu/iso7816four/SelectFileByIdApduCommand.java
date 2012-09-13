/*
 * Controlador Java de la Secretaría de Estado de Administraciones Públicas
 * para el DNI electrónico.
 *
 * El Controlador Java para el DNI electrónico es un proveedor de seguridad de JCA/JCE 
 * que permite el acceso y uso del DNI electrónico en aplicaciones Java de terceros 
 * para la realización de procesos de autenticación, firma electrónica y validación 
 * de firma. Para ello, se implementan las funcionalidades KeyStore y Signature para 
 * el acceso a los certificados y claves del DNI electrónico, así como la realización 
 * de operaciones criptográficas de firma con el DNI electrónico. El Controlador ha 
 * sido diseñado para su funcionamiento independiente del sistema operativo final.
 * 
 * Copyright (C) 2012 Dirección General de Modernización Administrativa, Procedimientos 
 * e Impulso de la Administración Electrónica
 * 
 * Este programa es software libre y utiliza un licenciamiento dual (LGPL 2.1+
 * o EUPL 1.1+), lo cual significa que los usuarios podrán elegir bajo cual de las
 * licencias desean utilizar el código fuente. Su elección deberá reflejarse 
 * en las aplicaciones que integren o distribuyan el Controlador, ya que determinará
 * su compatibilidad con otros componentes.
 *
 * El Controlador puede ser redistribuido y/o modificado bajo los términos de la 
 * Lesser GNU General Public License publicada por la Free Software Foundation, 
 * tanto en la versión 2.1 de la Licencia, o en una versión posterior.
 * 
 * El Controlador puede ser redistribuido y/o modificado bajo los términos de la 
 * European Union Public License publicada por la Comisión Europea, 
 * tanto en la versión 1.1 de la Licencia, o en una versión posterior.
 * 
 * Debería recibir una copia de la GNU Lesser General Public License, si aplica, junto
 * con este programa. Si no, consúltelo en <http://www.gnu.org/licenses/>.
 * 
 * Debería recibir una copia de la European Union Public License, si aplica, junto
 * con este programa. Si no, consúltelo en <http://joinup.ec.europa.eu/software/page/eupl>.
 *
 * Este programa es distribuido con la esperanza de que sea útil, pero
 * SIN NINGUNA GARANTÍA; incluso sin la garantía implícita de comercialización
 * o idoneidad para un propósito particular.
 */
package es.gob.jmulticard.apdu.iso7816four;

import es.gob.jmulticard.apdu.CommandApdu;

/** APDU ISO 7816-4 de selecci&oacute;n de fichero por Id.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public final class SelectFileByIdApduCommand extends CommandApdu {

    /** Byte de instrucci&oacute;n de la APDU. */
    private static final byte INS_SELECT_FILE = (byte) 0xA4;

    /** Selecci&oacute;n por ID. */
    private static final byte SELECT_BY_ID = 0x00;

    /** Primera b&uacute;squeda. */
    private static final byte SEARCH_FIRST = 0x00;

    /** Crea una APDU ISO 7816-4 de selecci&oacute;n de fichero por Identificador.
     * @param cla Clase (CLA) de la APDU
     * @param fileId Identificador del fichero a seleccionar. Debe estar situado en el DF actual */
    public SelectFileByIdApduCommand(final byte cla, final byte[] fileId) {
        super(
    		cla,				// CLA
    		INS_SELECT_FILE, 	// INS
    		SELECT_BY_ID, 		// P1
    		SEARCH_FIRST,		// P2
    		fileId,				// Data
    		null				// Le
		);
        if (fileId == null || fileId.length != 2) {
            throw new IllegalArgumentException(
        		"El identificador de fichero debe tener exactamente dos octetos" //$NON-NLS-1$
    		);
        }
    }

}