package com.aitutor.ai; public interface AiProvider { AiResponse generate(AiRequest request); boolean available(); String name(); }
