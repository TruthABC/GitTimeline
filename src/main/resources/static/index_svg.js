var dataSet;
var xMax;
var svg, margin, width, height;
var x, y, z;
var stack, area, g;
var keys;

function initD3() {
    dataSet = [];
    $("#preview").html("");

    svg = d3.select("#preview");
    margin = {top: 20, right: 20, bottom: 30, left: 50};
    width = svg.attr("width") - margin.left - margin.right;
    height = svg.attr("height") - margin.top - margin.bottom;

    x = d3.scaleLinear().range([0, width]);
    y = d3.scaleLinear().range([height, 0]);
    z = d3.scaleOrdinal(d3.schemeCategory10);

    stack = d3.stack();

    area = d3.area()
        .x(function(d) { return x(d.data.commitIndex); })
        .y0(function(d) { return y(d[0]); })
        .y1(function(d) { return y(d[1]); });

    g = svg.append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    keys = ["Rank1", "Rank2", "Rank3", "Rank4", "Rank5", "Rest"];
}

function refreshD3(data) {
    if (xMax === undefined)
        return;

    //1.抽取数据
    //  计数
    var apiFrequencyList = [];
    var key;
    var total = 0;
    var rest = 0;
    for (key in data.apiFrequency) {
        if (data.apiFrequency.hasOwnProperty(key)) {
            apiFrequencyList.push({
                apiName: key,
                apiCount: data.apiFrequency[key]
            });
            total += data.apiFrequency[key];
        }
    }
    rest = total;
    if (total === 0) {
        return;
    }

    //2.倒叙排序
    apiFrequencyList.sort(function(a, b) {
        return b.apiCount - a.apiCount;
    });

    //3.提取排序的前五
    var dataUnit = {
        commitIndex: data.commitIndex,
        counts: {}
    };
    var i;
    for (i = 0; i < apiFrequencyList.length && i < 5; i++) {
        dataUnit.counts[keys[i]] = apiFrequencyList[i].apiCount / total;
        rest -= apiFrequencyList[i].apiCount;
    }
    for (;i < 5; i++) {
        dataUnit.counts[keys[i]] = 0;
    }
    dataUnit.counts[keys[5]] = rest / total;

    //4.插入到总的数据集中
    dataSet.push(dataUnit);

    //5.变换svg
    x.domain([1, xMax]);
    z.domain(keys);
    stack.keys(keys);

    var layer = g.selectAll(".layer")
        .data(stack(dataSet))
        .enter().append("g")
        .attr("class", "layer");

    layer.append("path")
        .attr("class", "area")
        .style("fill", function(d) { return z(d.key); })
        .attr("d", area);

    layer.filter(function(d) { return d[d.length - 1][1] - d[d.length - 1][0] > 0.01; })
        .append("text")
        .attr("x", width - 6)
        .attr("y", function(d) { return y((d[d.length - 1][0] + d[d.length - 1][1]) / 2); })
        .attr("dy", ".35em")
        .style("font", "10px sans-serif")
        .style("text-anchor", "end")
        .text(function(d) { return d.key; });

    g.append("g")
        .attr("class", "axis axis--x")
        .attr("transform", "translate(0," + height + ")")
        .call(d3.axisBottom(x));

    g.append("g")
        .attr("class", "axis axis--y")
        .call(d3.axisLeft(y).ticks(10, "%"));

}
