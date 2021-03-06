/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * You may contact the copyright holder at: soporte.afirma@seap.minhap.es
 */

package es.gob.afirma.standalone.protocol;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.CertificateEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.callback.PasswordCallback;

import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.misc.protocol.UrlParametersToSelectCert;
import es.gob.afirma.keystores.AOCertificatesNotFoundException;
import es.gob.afirma.keystores.AOKeyStore;
import es.gob.afirma.keystores.AOKeyStoreDialog;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.AOKeyStoreManagerFactory;
import es.gob.afirma.keystores.filters.CertFilterManager;
import es.gob.afirma.keystores.filters.CertificateFilter;
import es.gob.afirma.standalone.crypto.CypherDataManager;

final class ProtocolInvocationLauncherSelectCert {

	private static final String RESULT_CANCEL = "CANCEL"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private ProtocolInvocationLauncherSelectCert() {
		// No instanciable
	}

	static String processSelectCert(final UrlParametersToSelectCert options, final boolean bySocket)
			throws SocketOperationException {

		if (options == null) {
			LOGGER.severe("Las opciones de firma son nulas"); //$NON-NLS-1$
			ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.ERROR_NULL_URI);
			if (!bySocket) {
				throw new SocketOperationException(ProtocolInvocationLauncherErrorManager.ERROR_NULL_URI);
			}
			return ProtocolInvocationLauncherErrorManager
					.getErrorMessage(ProtocolInvocationLauncherErrorManager.ERROR_NULL_URI);
		}

		if (!ProtocolInvocationLauncher.MAX_PROTOCOL_VERSION_SUPPORTED.support(options.getMinimumVersion())) {
			LOGGER.severe(String.format(
					"Version de protocolo no soportada (%1s). Version actual: %s2. Hay que actualizar la aplicacion.", //$NON-NLS-1$
					options.getMinimumVersion(), ProtocolInvocationLauncher.MAX_PROTOCOL_VERSION_SUPPORTED));
			ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.ERROR_UNSUPPORTED_PROCEDURE);
			return ProtocolInvocationLauncherErrorManager
					.getErrorMessage(ProtocolInvocationLauncherErrorManager.ERROR_UNSUPPORTED_PROCEDURE);
		}

		final AOKeyStore aoks = AOKeyStore.getKeyStore(options.getDefaultKeyStore());
		if (aoks == null) {
			LOGGER.severe("No hay un KeyStore asociado al valor: " + options.getDefaultKeyStore()); //$NON-NLS-1$
			ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.ERROR_CANNOT_FIND_KEYSTORE);
			if (!bySocket) {
				throw new SocketOperationException(ProtocolInvocationLauncherErrorManager.ERROR_CANNOT_FIND_KEYSTORE);
			}
			return ProtocolInvocationLauncherErrorManager
					.getErrorMessage(ProtocolInvocationLauncherErrorManager.ERROR_CANNOT_FIND_KEYSTORE);
		}

		final String aoksLib = options.getDefaultKeyStoreLib();
		final CertFilterManager filterManager = new CertFilterManager(options.getExtraParams());
		final List<CertificateFilter> filters = filterManager.getFilters();
		final boolean mandatoryCertificate = filterManager.isMandatoryCertificate();
		final PrivateKeyEntry pke;

		if (options.getSticky() && ProtocolInvocationLauncher.getStickyKeyEntry() != null) {

			LOGGER.info("Se usa Sticky Signature y tenemos valor de clave privada"); //$NON-NLS-1$
			pke = ProtocolInvocationLauncher.getStickyKeyEntry();

		} else {

			final PasswordCallback pwc = aoks.getStorePasswordCallback(null);
			final AOKeyStoreManager ksm;
			try {
				ksm = AOKeyStoreManagerFactory.getAOKeyStoreManager(
						aoks, // Store
						aoksLib, // Lib
						null, // Description
						pwc, // PasswordCallback
						null // Parent
				);
			} catch (final Exception e3) {
				LOGGER.severe("Error obteniendo el AOKeyStoreManager: " + e3); //$NON-NLS-1$
				ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.ERROR_CANNOT_ACCESS_KEYSTORE);
				if (!bySocket) {
					throw new SocketOperationException(ProtocolInvocationLauncherErrorManager.ERROR_CANNOT_ACCESS_KEYSTORE);
				}
				return ProtocolInvocationLauncherErrorManager
						.getErrorMessage(ProtocolInvocationLauncherErrorManager.ERROR_CANNOT_ACCESS_KEYSTORE);
			}

			LOGGER.info("Obtenido gestor de almacenes de claves: " + ksm); //$NON-NLS-1$

			try {
				ServiceInvocationManager.focusApplication();
				final AOKeyStoreDialog dialog = new AOKeyStoreDialog(
					ksm,
					null,
					true,
					true, // showExpiredCertificates
					true, // checkValidity
					filters,
					mandatoryCertificate
				);
				dialog.allowOpenExternalStores(filterManager.isExternalStoresOpeningAllowed());
				dialog.show();
				pke = ksm.getKeyEntry(dialog.getSelectedAlias());

				if (options.getSticky()) {
					ProtocolInvocationLauncher.setStickyKeyEntry(pke);
				} else {
					ProtocolInvocationLauncher.setStickyKeyEntry(null);
				}
			}
			catch (final AOCancelledOperationException e) {
				LOGGER.severe("Operacion cancelada por el usuario: " + e); //$NON-NLS-1$
				if (!bySocket) {
					throw new SocketOperationException(getResultCancel());
				}
				return getResultCancel();
			}
			catch (final AOCertificatesNotFoundException e) {
				LOGGER.severe("No hay certificados validos en el almacen: " + e); //$NON-NLS-1$
				ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.ERROR_NO_CERTIFICATES_KEYSTORE);
				if (!bySocket) {
					throw new SocketOperationException(ProtocolInvocationLauncherErrorManager.ERROR_NO_CERTIFICATES_KEYSTORE);
				}
				return ProtocolInvocationLauncherErrorManager
						.getErrorMessage(ProtocolInvocationLauncherErrorManager.ERROR_NO_CERTIFICATES_KEYSTORE);
			}
			catch (final Exception e) {
				LOGGER.severe("Error al mostrar el dialogo de seleccion de certificados: " + e); //$NON-NLS-1$
				ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.ERROR_CANNOT_ACCESS_KEYSTORE);
				if (!bySocket) {
					throw new SocketOperationException(ProtocolInvocationLauncherErrorManager.ERROR_CANNOT_ACCESS_KEYSTORE);
				}
				return ProtocolInvocationLauncherErrorManager
						.getErrorMessage(ProtocolInvocationLauncherErrorManager.ERROR_CANNOT_ACCESS_KEYSTORE);
			}

		}

		// Concatenamos el certificado utilizado para firmar y la firma con un
		// separador para que la pagina pueda recuperar ambos
		byte[] certEncoded;
		try {
			certEncoded = pke.getCertificateChain()[0].getEncoded();
		} catch (final CertificateEncodingException e) {
			LOGGER.severe("Error en la decodificacion del certificado de firma: " + e); //$NON-NLS-1$
			ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.ERROR_DECODING_CERTIFICATE);
			if (!bySocket) {
				throw new SocketOperationException(ProtocolInvocationLauncherErrorManager.ERROR_DECODING_CERTIFICATE);
			}
			return ProtocolInvocationLauncherErrorManager
					.getErrorMessage(ProtocolInvocationLauncherErrorManager.ERROR_DECODING_CERTIFICATE);
		}

		String dataToSend;
		if (options.getDesKey() != null) {
			try {
				// El CipherData devuelve los datos directamente en Base64
				dataToSend = CypherDataManager.cipherData(certEncoded, options.getDesKey());
			} catch (final Exception e) {
				LOGGER.severe("Error en el cifrado de los datos a enviar: " + e); //$NON-NLS-1$
				ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.ERROR_ENCRIPTING_DATA);
				if (!bySocket) {
					throw new SocketOperationException(ProtocolInvocationLauncherErrorManager.ERROR_ENCRIPTING_DATA);
				}
				return ProtocolInvocationLauncherErrorManager
						.getErrorMessage(ProtocolInvocationLauncherErrorManager.ERROR_ENCRIPTING_DATA);
			}
		} else {
			LOGGER.warning(
					"Se omite el cifrado de los datos resultantes por no haberse proporcionado una clave de cifrado" //$NON-NLS-1$
			);
			dataToSend = Base64.encode(certEncoded, true);
		}

		if (options.getStorageServletUrl() != null) {
			// Enviamos el certificado al servicio remoto de intercambio y detenemos la espera
			// activa si se encontraba vigente
			synchronized (IntermediateServerUtil.getUniqueSemaphoreInstance()) {
				final Thread waitingThread = ProtocolInvocationLauncher.getActiveWaitingThread();
				if (waitingThread != null) {
					waitingThread.interrupt();
				}
				try {
					IntermediateServerUtil.sendData(dataToSend, options.getStorageServletUrl().toString(), options.getId());
				}
				catch (final Exception e) {
					LOGGER.log(Level.SEVERE, "Error al enviar los datos al servidor", e); //$NON-NLS-1$
					ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.ERROR_SENDING_SIGNATURE);
					return ProtocolInvocationLauncherErrorManager.getErrorMessage(ProtocolInvocationLauncherErrorManager.ERROR_SENDING_SIGNATURE);
				}
			}
		}
		else {
			LOGGER.info(
				"Se omite el envio por red del resultado por no haberse proporcionado una URL de destino" //$NON-NLS-1$
			);
		}

		return dataToSend;
	}

	public static String getResultCancel() {
		return RESULT_CANCEL;
	}
}
