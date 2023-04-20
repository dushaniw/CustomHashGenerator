/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.custom.mediaton;

import org.apache.axiom.om.OMNode;
import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.mediator.cache.CachingConstants;
import org.wso2.carbon.mediator.cache.CachingException;
import org.wso2.carbon.mediator.cache.digest.REQUESTHASHGenerator;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class ExtendedREQUESTHASHGenerator extends REQUESTHASHGenerator {
    String[] headersToExclude = {""};

    @Override
    public void init(Map<String, Object> properties) {
        headersToExclude = (String[]) properties.get("headers-to-exclude");
    }

    /**
     * This is the implementation of the getDigest method and will implement the Extended DOMHASH algorithm based HTTP
     * request identifications. This will consider To address of the request, HTTP headers and XML Payload in generating
     * the digets. So, in effect this will uniquely identify the HTTP request with the same To address, Headers and
     * Payload. This includes the customization to exclude certain headers from the hashing methods by using
     * the <headersToExcludeInHash/> property.
     *
     * @param msgContext - MessageContext on which the XML node identifier will be generated
     * @return Object representing the DOMHASH value of the normalized XML node
     * @throws CachingException if there is an error in generating the digest key
     * @see org.wso2.caching.digest.DigestGenerator #getDigest(org.apache.axis2.context.MessageContext)
     */
    public String getDigest(MessageContext msgContext) throws CachingException {
        //Do not change following code
        OMNode body = msgContext.getEnvelope().getBody();
        String toAddress = null;
        if (msgContext.getTo() != null) {
            toAddress = msgContext.getTo().getAddress();
        }
        String[]  permanentlyExcludedHeaders = CachingConstants.PERMANENTLY_EXCLUDED_HEADERS;
        Map<String, String> headers = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        headers.putAll((Map<String, String>)msgContext.getProperty(MessageContext.TRANSPORT_HEADERS));
        //remove permanently excluded headers from hashing methods
        for (String excludedHeader : permanentlyExcludedHeaders) {
            headers.remove(excludedHeader);
        }
        //-----------Customization code starts here
        //Remove headers to exclude from hashing methods
        for (String header : headersToExclude) {
            headers.remove(header);
        }
        //-----------Customization code ends here
        //Do not change following code
        if (body != null) {
            byte[] digest = null;
            if (toAddress != null) {
                digest = getDigest(body, toAddress, headers, MD5_DIGEST_ALGORITHM);
            } else {
                digest = getDigest(body, MD5_DIGEST_ALGORITHM);
            }
            return digest != null ? getStringRepresentation(digest) : null;
        } else {
            return null;
        }
    }
}
