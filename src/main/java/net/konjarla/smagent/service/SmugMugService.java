package net.konjarla.smagent.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.smagent.model.SMAgentRecord;
import net.konjarla.smugmug.client.OAuth1HttpClient;
import net.konjarla.smugmug.client.api.Nodes;
import net.konjarla.smugmug.client.api.Search;
import net.konjarla.smugmug.model.SMNode;
import net.konjarla.smugmug.model.SMUser;
import net.konjarla.smugmug.model.SearchParams;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author: Srikanth Konjarla
 */

@Service
@AllArgsConstructor
@Slf4j
public class SmugMugService {
    @NonNull
    private OAuth1HttpClient oAuth1HttpClient;

    @NonNull
    private SMUser smugMugUser;

    public void findFolders(String text) {
        SearchParams searchParams = SearchParams.builder()
                .scope(smugMugUser.getUri())
                .build();

        List<SMNode> folders = Search.searchForNodes(oAuth1HttpClient, searchParams, text)
                .getNodes()
                .stream()
                .filter(node -> {
                    return node.getType().equals("Folder");
                })
                .map(node -> {
                    node.getUri();
                    return node;
                })
                .toList();
    }

    public List<SMAgentRecord.AlbumInfo> getAllAlbums() {
        List<SMAgentRecord.AlbumInfo> albums = new ArrayList<>();
        Queue<SMNode> queue = new LinkedList<>(Nodes.getRootNodes(oAuth1HttpClient));
        while (!queue.isEmpty()) {
            SMNode node = queue.poll();
            if (node.getType().equals("Album")) {
                albums.add(new SMAgentRecord.AlbumInfo(Nodes.getAlbumKeyByNodeId(oAuth1HttpClient, node.getNodeID()), node.getName()));
            } else if (node.getType().equals("Folder")) {
                List<SMNode> var001 = Nodes.getNodes(oAuth1HttpClient, node.getNodeID());
                if (var001 != null) {
                    queue.addAll(var001);
                }
            }
        }
        return albums;
    }
}
