package emissary.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import emissary.core.DataObjectFactory;
import emissary.core.IBaseDataObject;

/**
 * This class takes a SessionParser and produces data objects from the sessions coming out of the session parser.
 */
public class SessionProducer {

    public static final String ALT_VIEW_PARAM_PREFIX = "ALT_VIEW_";

    public static final String PARSER_ERROR = "This session could not be completely parsed. The format was bad.";

    protected SessionParser sp;
    // protected String myKey;
    protected int numSessions = 0;
    protected List<String> initialForms;

    protected boolean firstSession = true;

    /**
     * Creates a SessionProducer
     *
     * @param sp The SessionParser, which has parsed theContent.
     * @param myKey Value to stick into the transform history, obsolete
     * @param initialForms Forms to be preloaded onto the form stack.
     */
    public SessionProducer(final SessionParser sp, final String myKey, final List<String> initialForms) {
        this.sp = sp;
        // this.myKey = myKey;
        this.initialForms = initialForms;
    }

    /**
     * Creates a SessionProducer with one initial form
     *
     * @param sp The SessionParser, which has parsed theContent.
     * @param initialForm Form value to be preloaded onto the form stack.
     */
    public SessionProducer(final SessionParser sp, final String initialForm) {
        this.sp = sp;
        this.initialForms = new ArrayList<String>();
        this.initialForms.add(initialForm);
    }

    public IBaseDataObject createAndLoadDataObject(final DecomposedSession session, final String defaultSessionName) {
        final byte[] theHeader = session.getHeader();
        final byte[] theFooter = session.getFooter();
        final byte[] theData = session.getData();
        final String classification = session.getClassification();
        final Map<String, Collection<Object>> metadata = session.getMetaData();

        String sName = null;
        if (sp != null) {
            sName = sp.getSessionName(session);
        }
        if (sName == null) {
            sName = defaultSessionName;
        }

        final IBaseDataObject dataObject = DataObjectFactory.getInstance(new Object[] {theData, sName});

        // Pop default form if we have something to say
        if (initialForms != null && initialForms.size() > 0) {
            dataObject.popCurrentForm();
            // Add our stuff to the form stack
            for (int j = initialForms.size() - 1; j >= 0; j--) {
                dataObject.pushCurrentForm(initialForms.get(j));
            }
        }

        final List<String> sessionForms = session.getInitialForms();
        if (sessionForms != null && sessionForms.size() > 0) {
            dataObject.popCurrentForm();
            for (int j = sessionForms.size() - 1; j >= 0; j--) {
                dataObject.pushCurrentForm(sessionForms.get(j));
            }
        }

        if (classification != null) {
            dataObject.setClassification(classification);
        }

        if (theHeader != null) {
            dataObject.setHeader(theHeader);
        }

        if (theFooter != null) {
            dataObject.setFooter(theFooter);
        }

        if (metadata != null) {
            // Look for alternate view data (use iterator so we can remove)
            final Iterator<Map.Entry<String, Collection<Object>>> iter = metadata.entrySet().iterator();
            while (iter.hasNext()) {
                final Map.Entry<String, Collection<Object>> entry = iter.next();
                final String key = entry.getKey();
                if (key.startsWith(ALT_VIEW_PARAM_PREFIX)) {
                    final String baseViewName = key.substring(ALT_VIEW_PARAM_PREFIX.length());
                    int viewCounter = 0;
                    final Collection<Object> values = entry.getValue();
                    for (final Object valueItem : values) {
                        final String viewName = baseViewName + (viewCounter > 0 ? ("." + Integer.toString(viewCounter)) : "");
                        if (valueItem instanceof byte[]) {
                            dataObject.addAlternateView(viewName, (byte[]) valueItem);
                        } else {
                            dataObject.addAlternateView(viewName, valueItem.toString().getBytes());
                        }
                        viewCounter++;
                    }
                    iter.remove();
                }
            }

            // Add everything else as normal parameters
            dataObject.putParameters(metadata);
        }

        return dataObject;
    }

    /**
     * Produce the next session or throw ParserEOFException when out of data
     *
     * @param defaultSessionName name to use if we have nothing better
     * @return the IBaseDataObject implementation from the Factory
     */
    public IBaseDataObject getNextSession(final String defaultSessionName) throws ParserException {
        final DecomposedSession d = sp.getNextSession();
        return createAndLoadDataObject(d, defaultSessionName);
    }
}
