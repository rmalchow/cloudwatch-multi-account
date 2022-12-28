package me.rand0m.cloudwatch.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.prometheus.client.exporter.common.TextFormat;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.rand0m.cloudwatch.services.UpdateService;

@RestController
public class ExportController {

	@Autowired
	private UpdateService updateService;

	@GetMapping(value = "/health")
	public String ok() {
		return "ok";
	}
	
	@GetMapping(value = "/metrics")
	public void get(HttpServletRequest req, HttpServletResponse res) throws IOException {

		OutputStream os = res.getOutputStream();
		Writer w = new OutputStreamWriter(os);

		TextFormat.writeFormat(TextFormat.CONTENT_TYPE_004, w, updateService.run().metricFamilySamples());
		
		w.flush();
		os.flush();

	}

}
