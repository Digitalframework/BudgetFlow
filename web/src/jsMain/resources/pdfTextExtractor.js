import * as pdfjsLib from "pdfjs-dist";

// Use local worker file
pdfjsLib.GlobalWorkerOptions.workerSrc = '/pdf.worker.min.mjs';

export function extractPdfLines(file) {
    console.log("extractPdfLines called", file);
    return file.arrayBuffer().then(function(arrayBuffer) {
        return pdfjsLib.getDocument({ data: arrayBuffer }).promise;
    }).then(function(pdf) {
        console.log("PDF loaded, pages:", pdf.numPages);
        var pagePromises = [];
        for (var i = 1; i <= pdf.numPages; i++) {
            pagePromises.push(
                pdf.getPage(i).then(function(page) {
                    return page.getTextContent();
                })
            );
        }
        return Promise.all(pagePromises);
    }).then(function(pageContents) {
        var allItems = [];
        pageContents.forEach(function(content, pageIndex) {
            content.items.forEach(function(item) {
                if (item.str && item.str.trim()) {
                    allItems.push({
                        page: pageIndex + 1,
                        x: Math.round(item.transform[4]),
                        y: Math.round(item.transform[5]),
                        str: item.str
                    });
                }
            });
        });

        var lineMap = {};
        allItems.forEach(function(item) {
            var key = item.page + "_" + item.y;
            if (!lineMap[key]) lineMap[key] = [];
            lineMap[key].push(item);
        });

        var lines = Object.entries(lineMap)
            .sort(function(a, b) {
                var pa = parseInt(a[0].split("_")[0]);
                var ya = parseInt(a[0].split("_")[1]);
                var pb = parseInt(b[0].split("_")[0]);
                var yb = parseInt(b[0].split("_")[1]);
                return pa !== pb ? pa - pb : yb - ya;
            })
            .map(function(entry) {
                var items = entry[1];
                items.sort(function(a, b) { return a.x - b.x; });
                return items.map(function(i) { return i.str; })
                    .join(" ").replace(/\s+/g, " ").trim();
            })
            .filter(Boolean);

        console.log("Extracted lines:", lines.length, lines.slice(0, 5));
        return lines;
    });
}