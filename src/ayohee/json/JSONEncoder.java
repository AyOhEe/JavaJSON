package ayohee.json;

import javax.lang.model.type.NullType;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

final class JSONEncoder {
    public static String ParseObject(Object data) {
        switch (data) {
            case NullType nullType:
                return "null";

            case Number number:
                return number.toString();

            case String string:
                return "\"" + _EscapeString(string) + "\"";

            case Boolean bool:
                return bool.toString();

            case Iterable<?> iterable:
                return ParseIterable(iterable);

            case Map<?, ?> map:
                //ensure that all keys are strings
                if (map.keySet().stream().anyMatch((k) -> !(k instanceof String))){
                    throw new IllegalArgumentException("JSONEncoder provided map with non-string keys");
                }

                return ParseMap((Map<String, ?>)map);

            default:
                throw new IllegalArgumentException("JSONEncoder provided non-serializable value");
        }
    }

    private static String ParseIterable(Iterable<?> data) {
        List<String> asStrings = StreamSupport.stream(data.spliterator(), false).map(JSONEncoder::ParseObject).collect(Collectors.toList());
        return "[" + String.join(",", asStrings) + "]";
    }
    private static String ParseMap(Map<String, ?> data) {
        List<String> asStrings = new ArrayList<>();
        data.forEach((k, v) -> asStrings.add("\"" + _EscapeString(k) + "\":" + ParseObject(v)));

        return "{" + String.join(",", asStrings) + "}";
    }

    //TODO escaping strings
    private static String _EscapeString(String str) {
        return str;
    }

    //TODO prettification
    public static String Prettify(String s) {
        return s;
    }
}
