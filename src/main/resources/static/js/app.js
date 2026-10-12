// ---------- config & state ----------

const API = '/api/v1';

const CHART_SERIES = [
    { key: 'waterLevel',   label: 'Water level', unit: 'cm' },
    { key: 'flowRate',     label: 'Flow rate',   unit: 'm³/s' },
    { key: 'rainfall',     label: 'Rainfall',    unit: 'mm/h' },
    { key: 'batteryLevel', label: 'Battery',     unit: '%' },
    { key: 'temperature',  label: 'Temperature', unit: '°C' }
];

let stations = [];
let selectedStationId = null;
let selectedSeries = 'waterLevel';
let chart = null;
let chartMeasurements = [];

// ---------- api ----------

function currentFormat() {
    return $('#format').val();
}

function cleanParams(params) {
    const result = {};
    for (const [key, value] of Object.entries(params || {})) {
        if (value !== null && value !== undefined && value !== '') {
            result[key] = value;
        }
    }
    return result;
}

function apiGet(path, params, fromXml) {
    const format = currentFormat();
    return $.ajax({
        url: API + path,
        data: cleanParams(params),
        dataType: format,
        headers: { Accept: 'application/' + format }
    }).then(
        data => format === 'xml' ? fromXml(data) : data,
        xhr => { throw toApiError(xhr, format); }
    );
}

function toApiError(xhr, format) {
    if (xhr.status === 0) {
        return { status: 0, message: 'Server not reachable' };
    }
    try {
        const body = format === 'xml'
            ? apiErrorFromXml($.parseXML(xhr.responseText))
            : JSON.parse(xhr.responseText);
        return { status: body.status, message: body.message };
    } catch (e) {
        return { status: xhr.status, message: 'Request failed (HTTP ' + xhr.status + ')' };
    }
}

function getStations(filters) {
    return apiGet('/stations', filters, xml => {
        const list = listFromXml(xml, 'stations', 'station', stationFromXml);
        return { stations: list.items, totalCount: list.totalCount };
    });
}

function getLatestMeasurement(stationId) {
    return apiGet('/stations/' + stationId + '/measurements/latest', {},
        xml => measurementFromXml($(xml).children('measurement')));
}

function getMeasurements(stationId, filters) {
    return apiGet('/stations/' + stationId + '/measurements', filters, xml => {
        const list = listFromXml(xml, 'measurements', 'measurement', measurementFromXml);
        return { measurements: list.items, totalCount: list.totalCount };
    });
}

function getStatistics(stationId, filters) {
    return apiGet('/stations/' + stationId + '/statistics', filters,
        xml => statisticsFromXml($(xml).children('statistics')));
}

function getAlerts(filters) {
    return apiGet('/alerts', filters, xml => {
        const list = listFromXml(xml, 'alerts', 'alert', alertFromXml);
        return { alerts: list.items, totalCount: list.totalCount };
    });
}

// ---------- xml ----------

function xmlText(element, name) {
    const child = $(element).children(name);
    return child.length && child.text() !== '' ? child.text() : null;
}

function xmlNumber(element, name) {
    const text = xmlText(element, name);
    return text === null ? null : parseFloat(text);
}

function xmlBoolean(element, name) {
    return xmlText(element, name) === 'true';
}

function listFromXml(xml, rootName, childName, convert) {
    const root = $(xml).children(rootName);
    return {
        items: root.children(childName).map((i, el) => convert(el)).get(),
        totalCount: parseInt(root.attr('totalCount'))
    };
}

function locationFromXml(el) {
    const location = $(el).children('location');
    return {
        latitude: xmlNumber(location, 'latitude'),
        longitude: xmlNumber(location, 'longitude')
    };
}

function stationFromXml(el) {
    return {
        id: xmlText(el, 'id'),
        name: xmlText(el, 'name'),
        river: xmlText(el, 'river'),
        location: locationFromXml(el),
        normalWaterLevel: xmlNumber(el, 'normalWaterLevel'),
        warningWaterLevel: xmlNumber(el, 'warningWaterLevel'),
        criticalWaterLevel: xmlNumber(el, 'criticalWaterLevel'),
        isActive: xmlBoolean(el, 'isActive'),
        currentWarningLevel: xmlText(el, 'currentWarningLevel')
    };
}

function measurementFromXml(el) {
    return {
        stationId: xmlText(el, 'stationId'),
        timestamp: xmlText(el, 'timestamp'),
        waterLevel: xmlNumber(el, 'waterLevel'),
        flowRate: xmlNumber(el, 'flowRate'),
        rainfall: xmlNumber(el, 'rainfall'),
        temperature: xmlNumber(el, 'temperature'),
        batteryLevel: xmlNumber(el, 'batteryLevel'),
        status: xmlText(el, 'status'),
        warningLevel: xmlText(el, 'warningLevel')
    };
}

function statisticsFromXml(el) {
    return {
        minWaterLevel: xmlNumber(el, 'minWaterLevel'),
        maxWaterLevel: xmlNumber(el, 'maxWaterLevel'),
        meanWaterLevel: xmlNumber(el, 'meanWaterLevel'),
        meanFlowRate: xmlNumber(el, 'meanFlowRate'),
        totalRainfall: xmlNumber(el, 'totalRainfall'),
        measurementCount: xmlNumber(el, 'measurementCount'),
        warningPeriodCount: xmlNumber(el, 'warningPeriodCount'),
        criticalPeriodCount: xmlNumber(el, 'criticalPeriodCount')
    };
}

function alertFromXml(el) {
    return {
        stationId: xmlText(el, 'stationId'),
        name: xmlText(el, 'name'),
        river: xmlText(el, 'river'),
        location: locationFromXml(el),
        timestamp: xmlText(el, 'timestamp'),
        normalWaterLevel: xmlNumber(el, 'normalWaterLevel'),
        warningWaterLevel: xmlNumber(el, 'warningWaterLevel'),
        criticalWaterLevel: xmlNumber(el, 'criticalWaterLevel'),
        waterLevel: xmlNumber(el, 'waterLevel'),
        warningLevel: xmlText(el, 'warningLevel')
    };
}

function apiErrorFromXml(xml) {
    const el = $(xml).children('error');
    return { status: xmlNumber(el, 'status'), message: xmlText(el, 'message') };
}

// ---------- formatting ----------

function formatValue(value, unit, digits = 2) {
    if (value === null || value === undefined) {
        return '–';
    }
    return value.toFixed(digits) + (unit ? ' ' + unit : '');
}

function formatTime(isoString) {
    return new Date(isoString).toLocaleString();
}

function rangeLabel(range) {
    if (!range.from && !range.to) return 'entire stored history';
    const from = range.from ? formatTime(range.from) : 'start';
    const to = range.to ? formatTime(range.to) : 'now';
    return from + ' – ' + to;
}

// ---------- ui helpers ----------

function levelDot(level) {
    return $('<span class="dot">').addClass('level-' + level).attr('title', level);
}

function levelBadge(level) {
    return $('<span class="badge">').addClass('level-' + level).text(level);
}

function tile(label, value) {
    return $('<div>').append($('<small>').text(label), $('<strong>').text(value));
}

function withSpinner(selector, promise, enabled) {
    if (!enabled) return promise;
    $(selector).attr('aria-busy', 'true');
    return promise.always(() => $(selector).removeAttr('aria-busy'));
}

function showError(error) {
    $('#message').addClass('error').text(error.message);
}

function clearMessage() {
    $('#message').removeClass('error').text('');
}

// ---------- filters ----------

function toIsoOrNull(localValue) {
    return localValue ? new Date(localValue).toISOString() : null;
}

function currentRange() {
    return {
        from: toIsoOrNull($('#filterFrom').val()),
        to: toIsoOrNull($('#filterTo').val())
    };
}

function currentTableFilters() {
    return {
        ...currentRange(),
        warningLevel: $('#filterLevel').val(),
        limit: $('#filterLimit').val()
    };
}

// ---------- stations ----------

function loadStations() {
    return getStations().then(response => {
        stations = response.stations;
        renderStationList();
        $('#lastUpdate').text('Updated ' + new Date().toLocaleTimeString());

        if (selectedStationId === null && stations.length > 0) {
            selectStation(stations[0].id);
        }
    });
}

function renderStationList() {
    const list = $('<ul>');

    for (const station of stations) {
        const link = $('<a href="#">')
            .attr('data-id', station.id)
            .append(levelDot(station.currentWarningLevel))
            .append(' ', $('<span>').text(station.name))
            .append($('<br>'), $('<small>').text(station.river));

        if (station.id === selectedStationId) {
            link.attr('aria-current', 'true');
        }
        list.append($('<li>').append(link));
    }

    $('#stationList').empty().append($('<nav>').append(list));
}

function selectStation(id) {
    selectedStationId = id;
    const station = stations.find(s => s.id === id);

    $('#stationTitle').text(station.name + ' (' + station.river + ')');
    renderStationList();
    loadStationDetails();
}

// ---------- station details ----------

function loadStationDetails(clear = true) {
    const id = selectedStationId;
    const range = currentRange();
    if (clear) $('#latest, #statistics, #history').empty();

    withSpinner('#latest', getLatestMeasurement(id), clear)
        .then(measurement => { if (id === selectedStationId) renderLatest(measurement); })
        .catch(error => { if (id === selectedStationId) renderLatestError(error); });

    withSpinner('#statistics', getStatistics(id, range), clear)
        .then(statistics => { if (id === selectedStationId) renderStatistics(statistics, range); })
        .catch(showError);

    loadHistory(clear);
    loadChart();
}

function renderLatest(m) {
    const card = $('<article>').append(
        $('<header>').append(
            $('<strong>').text('Latest measurement '),
            levelBadge(m.warningLevel),
            $('<br>'),
            $('<small>').text(formatTime(m.timestamp) + ' · ' + m.status)
        ),
        $('<div class="tiles">').append(
            tile('Water level', formatValue(m.waterLevel, 'cm')),
            tile('Flow rate', formatValue(m.flowRate, 'm³/s')),
            tile('Rainfall', formatValue(m.rainfall, 'mm/h')),
            tile('Temperature', formatValue(m.temperature, '°C')),
            tile('Battery', formatValue(m.batteryLevel, '%', 0))
        )
    );
    $('#latest').empty().append(card);
}

function renderLatestError(error) {
    $('#latest').empty().append($('<article>').append($('<small>').text(error.message)));
}

function renderStatistics(s, range) {
    const card = $('<article>').append(
        $('<header>').append(
            $('<strong>').text('Statistics'),
            $('<small>').text(' · ' + rangeLabel(range))
        ),
        $('<div class="tiles">').append(
            tile('Min water level', formatValue(s.minWaterLevel, 'cm')),
            tile('Max water level', formatValue(s.maxWaterLevel, 'cm')),
            tile('Mean water level', formatValue(s.meanWaterLevel, 'cm')),
            tile('Mean flow rate', formatValue(s.meanFlowRate, 'm³/s')),
            tile('Total rainfall', formatValue(s.totalRainfall, 'mm')),
            tile('Warning periods', formatValue(s.warningPeriodCount, '', 0)),
            tile('Critical periods', formatValue(s.criticalPeriodCount, '', 0)),
            tile('Measurements', formatValue(s.measurementCount, '', 0))
        )
    );
    $('#statistics').empty().append(card);
}

// ---------- chart ----------

function loadChart() {
    const id = selectedStationId;
    getMeasurements(id, currentRange())
        .then(response => {
            if (id !== selectedStationId) return;
            chartMeasurements = response.measurements.slice().reverse();
            renderChart();
        })
        .catch(showError);
}

function renderChartTabs() {
    const buttons = CHART_SERIES.map(series =>
        $('<button type="button">')
            .attr('data-series', series.key)
            .toggleClass('secondary outline', series.key !== selectedSeries)
            .text(series.label));

    $('#chartTabs').empty().append($('<div role="group">').append(buttons));
}

function renderChart() {
    const series = CHART_SERIES.find(s => s.key === selectedSeries);
    const points = chartMeasurements.map(m => ({ x: Date.parse(m.timestamp), y: m[series.key] }));

    const datasets = [{
        label: series.label + ' (' + series.unit + ')',
        data: points,
        borderColor: '#1c7ed6',
        borderWidth: 1.5,
        pointRadius: 0
    }];

    if (series.key === 'waterLevel' && points.length > 0) {
        const station = stations.find(s => s.id === selectedStationId);
        const start = points[0].x;
        const end = points[points.length - 1].x;
        datasets.push(thresholdLine('Warning', station.warningWaterLevel, start, end, '#f08c00'));
        datasets.push(thresholdLine('Critical', station.criticalWaterLevel, start, end, '#e03131'));
    }

    if (chart) chart.destroy();
    chart = new Chart($('#chart')[0], {
        type: 'line',
        data: { datasets: datasets },
        options: {
            parsing: false,
            animation: false,
            aspectRatio: 3,
            interaction: { mode: 'nearest', axis: 'x', intersect: false },
            scales: { x: { type: 'time' } },
            plugins: { decimation: { enabled: true, algorithm: 'min-max' } }
        }
    });
}

function thresholdLine(label, value, start, end, color) {
    return {
        label: label,
        data: [{ x: start, y: value }, { x: end, y: value }],
        borderColor: color,
        borderDash: [6, 4],
        borderWidth: 1,
        pointRadius: 0
    };
}

// ---------- history ----------

function loadHistory(clear = true) {
    const id = selectedStationId;
    withSpinner('#history', getMeasurements(id, currentTableFilters()), clear)
        .then(response => { if (id === selectedStationId) renderHistory(response); })
        .catch(showError);
}

function renderHistory(response) {
    const station = stations.find(s => s.id === selectedStationId);
    const headers = ['Station', 'Time', 'Water level', 'Flow rate', 'Rainfall',
        'Battery', 'Status', 'Warning level'];

    const rows = response.measurements.map(m => $('<tr>').append(
        $('<td>').text(station ? station.name : m.stationId),
        $('<td>').text(formatTime(m.timestamp)),
        $('<td>').text(formatValue(m.waterLevel, 'cm')),
        $('<td>').text(formatValue(m.flowRate, 'm³/s')),
        $('<td>').text(formatValue(m.rainfall, 'mm/h')),
        $('<td>').text(formatValue(m.batteryLevel, '%', 0)),
        $('<td>').text(m.status),
        $('<td>').append(levelBadge(m.warningLevel))
    ));

    const table = $('<table>').append(
        $('<thead>').append($('<tr>').append(headers.map(h => $('<th>').text(h)))),
        $('<tbody>').append(rows)
    );

    $('#historySummary').text(
        'Measurement history (' + response.measurements.length + ' of ' + response.totalCount + ')');
    $('#history').empty().append($('<div class="overflow-auto">').append(table));
}

// ---------- alerts ----------

function loadAlerts() {
    getAlerts().then(renderAlerts).catch(showError);
}

function renderAlerts(response) {
    const alerts = response.alerts;
    const banner = $('#alertBanner').empty().prop('hidden', false);

    if (alerts.length === 0) {
        banner.append($('<article>').addClass('level-NORMAL').text('No active alerts'));
        return;
    }

    const items = alerts.map(alert => $('<li>').append(
        $('<a href="#">').attr('data-id', alert.stationId).append(
            levelBadge(alert.warningLevel),
            ' ',
            $('<strong>').text(alert.name),
            $('<span>').text(' (' + alert.river + ') · ' + formatValue(alert.waterLevel, 'cm'))
        )
    ));

    banner.append($('<article>').addClass('level-' + alerts[0].warningLevel).append(
        $('<strong>').text(alerts.length + (alerts.length === 1 ? ' active alert' : ' active alerts')),
        $('<ul>').append(items)
    ));
}

// ---------- init ----------

function refresh() {
    clearMessage();
    loadStations().catch(showError);
    loadAlerts();
    if (selectedStationId !== null) {
        loadStationDetails(false);
    }
}

$(function () {
    Chart.defaults.color = getComputedStyle(document.body).color;
    Chart.defaults.borderColor = 'rgba(128, 128, 128, 0.2)';
    renderChartTabs();

    $('#stationList').on('click', 'a', function (event) {
        event.preventDefault();
        selectStation($(this).data('id'));
    });

    $('#alertBanner').on('click', 'a', function (event) {
        event.preventDefault();
        selectStation($(this).data('id'));
    });

    $('#format').on('change', refresh);

    $('#rangeForm').on('submit', function (event) {
        event.preventDefault();
        clearMessage();
        loadStationDetails();
    });

    $('#resetRange').on('click', function () {
        $('#rangeForm')[0].reset();
        clearMessage();
        loadStationDetails();
    });

    $('#chartTabs').on('click', 'button', function () {
        selectedSeries = $(this).data('series');
        renderChartTabs();
        renderChart();
    });

    $('#filterLevel, #filterLimit').on('change', () => loadHistory());

    refresh();
    setInterval(refresh, 30000);
});
