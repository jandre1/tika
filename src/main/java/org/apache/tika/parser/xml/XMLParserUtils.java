/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.parser.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.metadata.Metadata;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.Namespace;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;

/**
 * XML parser utils
 */
public class XMLParserUtils {

    static Logger logger = Logger.getRootLogger();
    private SimpleNamespaceContext nsContext;

  
    public void concatOccurrence(Object xmlDoc, String xpath, String concatSep,
            Appendable chaineConcat) throws IOException {

        try {
            JDOMXPath xp = new JDOMXPath(xpath);
            if (nsContext != null) {
                xp.setNamespaceContext(nsContext);
            }
            List ls = xp.selectNodes(xmlDoc);
            Iterator i = ls.iterator();
            int j = 0;
            while (i.hasNext()) {
                j++;
                String text = "";
                Object obj = i.next();
                if (obj instanceof Element) {
                    Element elem = (Element) obj;
                    text = elem.getText().trim();
                } else if (obj instanceof Attribute) {
                    Attribute att = (Attribute) obj;
                    text = att.getValue().trim();
                } else if (obj instanceof Text) {
                    Text txt = (Text) obj;
                    text = txt.getText().trim();
                } else if (obj instanceof Comment) {
                    Comment com = (Comment) obj;
                    text = com.getText().trim();
                } else if (obj instanceof ProcessingInstruction) {
                    ProcessingInstruction pi = (ProcessingInstruction) obj;
                    text = pi.getData().trim();
                } else if (obj instanceof EntityRef) {
                    EntityRef er = (EntityRef) obj;
                    text = er.toString().trim();
                }
                if (StringUtils.isNotEmpty(text)) {
                    chaineConcat.append(text);
                    if (ls.size() == 1) {
                        return;
                    } else {
                        if (ls.size() != j) {
                            chaineConcat.append(' ').append(concatSep).append(
                                    ' ');
                        }
                    }
                }
            }
        } catch (JaxenException j) {
            logger.error(j.getMessage());
        }
    }

    public List getAllDocumentNs(org.jdom.Document doc) {
        List ls = new ArrayList();
        processChildren(doc.getRootElement(), ls);
        return ls;
    }

    private boolean exist(List nsLs, String nsUri) {
        if (nsLs.isEmpty())
            return false;
        for (Object nsL : nsLs) {
            if (nsL.equals(nsUri)) {
                return true;
            }
        }
        return false;
    }

    private void processChildren(Element elem, List ns) {
        Namespace nsCourent = elem.getNamespace();
        String nsUri = (nsCourent.getURI());
        if (!exist(ns, nsUri)) {
            ns.add(nsUri.trim());
        }
        List additionalNs = elem.getAdditionalNamespaces();
        if (!additionalNs.isEmpty())
            copyNsList(additionalNs, ns);
        if (elem.getChildren().size() > 0) {
            List elemChildren = elem.getChildren();
            for (Object anElemChildren : elemChildren) {
                processChildren((Element) anElemChildren, ns);
            }
        }
    }

    private void copyNsList(List nsElem, List nsRes) {
        for (Object aNsElem : nsElem) {
            nsRes.add(((Namespace) aNsElem).getURI().trim());
        }
    }

    public void extractContent(Document xmlDoc, String name, String xpath,
            Metadata metadata) {
        try {
            JDOMXPath xp = new JDOMXPath(xpath);
            if (nsContext != null) {
                xp.setNamespaceContext(nsContext);
            }
            List selectNodes = xp.selectNodes(xmlDoc);
            Iterator nodes = selectNodes.iterator();
            while (nodes.hasNext()) {
                Object node = nodes.next();
                if (node instanceof Element) {
                    Element elem = (Element) node;
                    if (StringUtils.isNotBlank(elem.getText())) {
                        metadata.add(name, elem.getText().trim());
                    }
                } else if (node instanceof Attribute) {
                    Attribute att = (Attribute) node;
                    metadata.add(name, att.getValue());
                } else if (node instanceof Text) {
                    Text text = (Text) node;
                    metadata.add(name, text.getText());
                } else if (node instanceof Comment) {
                    Comment com = (Comment) node;
                    metadata.add(name, com.getText());
                } else if (node instanceof ProcessingInstruction) {
                    ProcessingInstruction pi = (ProcessingInstruction) node;
                    metadata.add(name, pi.getData());
                } else if (node instanceof EntityRef) {
                    EntityRef er = (EntityRef) node;
                    metadata.add(name, er.toString());
                }
            }
        } catch (JaxenException e) {
            logger.error(e.getMessage());
        }

    }

    public void setXmlParserNameSpaceContext(SimpleNamespaceContext nsContext) {
        this.nsContext = nsContext;
    }
}
