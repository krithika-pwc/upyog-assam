import Urls from "../atoms/urls";
import { Request } from "../atoms/Utils/Request";

// This file defines the OBPSV2Services object, providing methods for creating, searching, and updating OBPSV2 resources through structured API requests.
export const OBPSV2Services= {
  
  create: (details) =>
    Request({
      url: Urls.obpsv2.create,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: {},
      auth: true,
    }),
    rtpcreate: (data, tenantId) =>
    Request({
      url: Urls.edcr.create,
      // data: data,
      multipartData: data,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: { tenantId },
      auth: true,
      multipartFormData: true
    }),
    anonymousCreate: (data, tenantId) =>
      Request({
        url: Urls.edcr.anonymousCreate,
        // data: data,
        multipartData: data,
        useCache: false,
        setTimeParam: false,
        userService: true,
        method: "POST",
        params: { tenantId },
        auth: false,
        multipartFormData: true
    }),

  update: (details) => 
    Request({
        url: Urls.obpsv2.update,
        data: details,
        useCache: false,
        setTimeParam: false,
        userService: true,
        method: "POST",
        params: {},
        auth: true,
    }),

  search: ({ tenantId, filters, auth }) =>
    Request({
      url: Urls.obpsv2.search,
      useCache: false,
      method: "POST",
      auth: auth === false ? auth : true,
      userService: auth === false ? auth : true,
      params: { tenantId, ...filters },
    })
};