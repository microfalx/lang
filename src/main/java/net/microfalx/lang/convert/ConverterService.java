package net.microfalx.lang.convert;

import net.microfalx.lang.Initializable;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.lang.service.Service;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A service supporting type conversion.
 */
@Provider
public class ConverterService implements Service, Initializable {

    /**
     * Returns a reference to t
     * @return
     */
    public static ConverterService getInstance() {
        return Service.lookup(ConverterService.class);
    }

    private JsonConverter jsonConverter = new GsonConverter();

    /**
     * Returns the JSON converter.
     *
     * @return a non-null instance
     */
    public JsonConverter getJsonConverter() {
        return jsonConverter;
    }

    /**
     * Changes the JSON converter.
     *
     * @param jsonConverter the new converter
     */
    public void setJsonConverter(JsonConverter jsonConverter) {
        requireNonNull(jsonConverter);
        this.jsonConverter = jsonConverter;
        if (jsonConverter instanceof Initializable) {
            ((Initializable) jsonConverter).initialize();
        }
        Types.converter = jsonConverter;
    }

    @Override
    public void initialize(Object... context) {
        setJsonConverter(new GsonConverter());
    }
}
