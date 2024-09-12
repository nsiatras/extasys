/*Copyright (c) 2008 Nikos Siatras

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.*/
package Extasys.Examples.TCPChatServer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;

/**
 *
 * @author Nikos Siatras - https://github.com/nsiatras
 */
public class MessageToken
{

    private final String fHeader;
    private final String fData;

    public MessageToken(String header, String data)
    {
        fHeader = header;
        fData = data;
    }

    /**
     * Serialize this Object to JSON
     *
     * @return
     */
    public String toJSON()
    {
        Gson gson = new Gson();
        HashMap<String, String> map = new HashMap<>();
        map.put("H", fHeader);
        map.put("D", fData);

        return gson.toJson(map);
    }

    public static MessageToken fromJSON(String jsonString)
    {
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, String>>()
        {
        }.getType();

        // Deserialize το JSON string σε HashMap
        HashMap<String, String> map = gson.fromJson(jsonString, type);

        return new MessageToken(map.get("H"), map.get("D"));
    }

    /**
     * Returns the Header
     *
     * @return
     */
    public String getHeader()
    {
        return fHeader;
    }

    /**
     * Returns the Data
     *
     * @return
     */
    public String getData()
    {
        return fData;
    }

}
